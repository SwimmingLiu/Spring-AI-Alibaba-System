package com.swimmingliu.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.swimmingliu.common.properties.DashscopeDeepseekProperties;
import com.swimmingliu.common.utils.FileUtil;
import com.swimmingliu.service.ChatClientService;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;

import static com.swimmingliu.common.constants.BaseConstants.CHAT_MEMORY_RETRIEVE_SIZE;
import static com.swimmingliu.common.constants.PromptConstants.*;
import static com.swimmingliu.common.constants.PromptParamConstants.CONTEXT_DOCUMENT;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service("deepseekChatClientService")
public class DeepseekChatClientServiceImpl implements ChatClientService {
    public final DashscopeDeepseekProperties deepseekProperties;
    public final ChatClient deepSeekChatClient;
    public final ChatMemory redisChatMemory;
    @Resource
    private FileUtil fileUtil;


    public DeepseekChatClientServiceImpl(DashscopeDeepseekProperties deepseekProperties,
                                         DashScopeChatModel chatModel,
                                         ChatMemory redisChatMemory) {
        this.deepseekProperties = deepseekProperties;
        this.redisChatMemory = redisChatMemory;
        DashScopeChatOptions dashScopeChatOptions = DashScopeChatOptions.builder()
                .withTemperature(this.deepseekProperties.getTemperature())
                .withMaxToken(this.deepseekProperties.getMaxTokens())
                .withModel(this.deepseekProperties.getChatModelName())
                .build();
        this.deepSeekChatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(new MessageChatMemoryAdvisor(this.redisChatMemory))
                // 实现 Logger 的 Advisor
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(dashScopeChatOptions)
                .defaultSystem(DEFAULT_SETTING_PROMPT)
                .build();
    }


    @Override
    public String ask(String question, String chatId) {
        return deepSeekChatClient.prompt(question)
                .advisors(x -> x
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE))
                .call()
                .content();
    }

    @Override
    public Flux<String> askStream(String question, String chatId) {
        return this.deepSeekChatClient.prompt(question)
                .advisors(x -> x
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE))
                .stream()
                .content();
    }

    @Override
    public String askWithFile(String question, String chatId, String fileUrl) throws IOException, InterruptedException {
        String documentText = fileUtil.getDocumentText(fileUrl);
        return deepSeekChatClient.prompt(question)
                .advisors(x -> x
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE))
                .system(systemSpec ->
                        systemSpec.text(DOCUMENT_RAG_PROMPT).param(CONTEXT_DOCUMENT, documentText)
                )
                .call()
                .content();
    }

    @Override
    public Flux<String> askStreamWithFile(String question, String chatId, String fileUrl) throws IOException, InterruptedException {
        String documentText = fileUtil.getDocumentText(fileUrl);
        return this.deepSeekChatClient.prompt(question)
                .advisors(x -> x
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE))
                .system(systemSpec ->
                        systemSpec.text(DOCUMENT_RAG_PROMPT).param(CONTEXT_DOCUMENT, documentText)
                )
                .stream()
                .content();
    }

    @Override
    public Boolean checkWebSearch(String question) {
        String content = deepSeekChatClient.prompt(question)
                .system(NEEDS_WEB_SEARCH_CHECK_PROMPT)
                .call()
                .content();
        return Boolean.parseBoolean(content);
    }
}