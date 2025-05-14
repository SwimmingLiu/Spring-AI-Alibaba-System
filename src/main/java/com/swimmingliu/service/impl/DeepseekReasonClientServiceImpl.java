package com.swimmingliu.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.swimmingliu.common.advisor.ReasoningContentAdvisor;
import com.swimmingliu.common.properties.DashscopeDeepseekProperties;
import com.swimmingliu.service.ChatClientService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static com.swimmingliu.common.constants.BaseConstants.DEFAULT_SETTING_PROMPT;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service("deepseekReasonClientService")
public class DeepseekReasonClientServiceImpl implements ChatClientService {
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
                .defaultAdvisors(new MessageChatMemoryAdvisor(this.redisChatMemory))
                // 输出Deepseek的思考过程
                .defaultAdvisors(this.reasoningContentAdvisor)
                // 实现 Logger 的 Advisor
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(dashScopeChatOptions)
                .defaultSystem(DEFAULT_SETTING_PROMPT)
                .build();
    }


    @Override
    public String ask(String question, String chatId) {
        return deepseekChatClient.prompt(question)
                .advisors(x -> x
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .call()
                .content();
    }

    @Override
    public Flux<String> askStream(String question, String chatId) {
        return this.deepseekChatClient.prompt(question)
                .advisors(x -> x
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .stream()
                .content();
    }
}