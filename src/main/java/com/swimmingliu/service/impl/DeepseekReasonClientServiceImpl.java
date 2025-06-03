package com.swimmingliu.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.swimmingliu.common.advisor.ReasoningContentAdvisor;
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
*   @description: deepseek-r1
*/


@Service("deepseekReasonClientService")
public class DeepseekReasonClientServiceImpl extends AbstractChatClientService implements ChatClientService {
    @Resource
    private FileUtil fileUtil;
    public final DashscopeDeepseekProperties deepseekProperties;
    public final ChatClient deepseekChatClient;
    public final ReasoningContentAdvisor reasoningContentAdvisor;
    public final ChatMemory redisChatMemory;

    public DeepseekReasonClientServiceImpl(DashscopeDeepseekProperties deepseekProperties,
                                           DashScopeChatModel chatModel,
                                           ChatMemory redisChatMemory) {
        this.deepseekProperties = deepseekProperties;
        this.redisChatMemory = redisChatMemory;
        this.reasoningContentAdvisor = new ReasoningContentAdvisor(1);
        DashScopeChatOptions dashScopeChatOptions = DashScopeChatOptions.builder()
                .withTemperature(this.deepseekProperties.getTemperature())
                .withMaxToken(this.deepseekProperties.getMaxTokens())
                .withModel(this.deepseekProperties.getReasonModelName())
                .build();
        this.deepseekChatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(this.redisChatMemory).build())
                // 输出Deepseek的思考过程
                .defaultAdvisors(this.reasoningContentAdvisor)
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