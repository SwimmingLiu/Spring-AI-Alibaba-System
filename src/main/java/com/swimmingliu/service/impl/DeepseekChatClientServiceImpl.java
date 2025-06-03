package com.swimmingliu.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.swimmingliu.common.constants.PromptConstants;
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

import static com.swimmingliu.common.constants.PromptConstants.DOCUMENT_RAG_PROMPT;
import static com.swimmingliu.common.constants.PromptParamConstants.CONTEXT_DOCUMENT;

/**
*   @author SwimmingLiu
*   @date 2025-06-03 16:28
*   @description: deepseek-v3
*/

@Service("deepseekChatClientService")
public class DeepseekChatClientServiceImpl extends AbstractChatClientService implements ChatClientService {
    @Resource
    private FileUtil fileUtil;
    public final DashscopeDeepseekProperties deepseekProperties;
    public final ChatClient deepseekChatClient;
    public final ChatMemory redisChatMemory;

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
        this.deepseekChatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(this.redisChatMemory).build())
                // 实现 Logger 的 Advisor
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(dashScopeChatOptions)
                .build();
    }


    @Override
    public String doAsk(String question, String chatId) {
        return deepseekChatClient.prompt(question)
                .advisors(x -> x
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .content();
    }

    @Override
    public Flux<String> doAskStream(String question, String chatId) {
        return this.deepseekChatClient.prompt(question)
                .advisors(x -> x
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    @Override
    public Boolean checkWebSearch(String question) {
        String content = deepseekChatClient.prompt(question)
                .system(PromptConstants.WEB_SEARCH_CHECK_PROMPT)
                .call()
                .content();
        return Boolean.parseBoolean(content);
    }

    @Override
    public String doAskWithFile(String question, String chatId, String fileUrl) throws IOException, InterruptedException {
        String documentText = fileUtil.getDocumentText(fileUrl);
        return deepseekChatClient.prompt(question)
                .advisors(x -> x
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                .system(systemSpec ->
                        systemSpec.text(DOCUMENT_RAG_PROMPT).param(CONTEXT_DOCUMENT, documentText)
                )
                .call()
                .content();
    }

    @Override
    public Flux<String> doAskStreamWithFile(String question, String chatId, String fileUrl) throws IOException, InterruptedException {
        String documentText = fileUtil.getDocumentText(fileUrl);
        return this.deepseekChatClient.prompt(question)
                .advisors(x -> x
                        .param(ChatMemory.CONVERSATION_ID, chatId))
                .system(systemSpec ->
                        systemSpec.text(DOCUMENT_RAG_PROMPT).param(CONTEXT_DOCUMENT, documentText)
                )
                .stream()
                .content();
    }
}