package com.swimmingliu.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.swimmingliu.common.properties.DashscopeDeepseekProperties;
import com.swimmingliu.rag.WebSearchRetriever;
import com.swimmingliu.rag.core.IQSSearchEngine;
import com.swimmingliu.rag.data.DataClean;
import com.swimmingliu.rag.join.ConcatenationDocumentJoiner;
import com.swimmingliu.rag.prompt.CustomContextQueryAugmenter;
import com.swimmingliu.service.ChatClientService;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.postretrieval.ranking.DocumentRanker;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static com.swimmingliu.common.constants.BaseConstants.CHAT_MEMORY_RETRIEVE_SIZE;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service("deepseekWebSearchReasonClientService")
public class DeepseekWebSearchReasonClientServiceImpl extends DeepseekReasonClientServiceImpl implements ChatClientService {

    private final QueryTransformer queryTransformer;

    private final QueryExpander queryExpander;

    private final PromptTemplate queryArgumentPromptTemplate;

    private final WebSearchRetriever webSearchRetriever;

    public DeepseekWebSearchReasonClientServiceImpl(DashscopeDeepseekProperties deepseekProperties,
                                                    DashScopeChatModel chatModel,
                                                    ChatMemory redisChatMemory,
                                                    DataClean dataCleaner,
                                                    QueryExpander queryExpander,
                                                    IQSSearchEngine searchEngine,
                                                    DocumentRanker documentRanker,
                                                    QueryTransformer queryTransformer,
                                                    @Qualifier("queryArgumentPromptTemplate") PromptTemplate queryArgumentPromptTemplate) {
        super(deepseekProperties, chatModel, redisChatMemory);
        this.queryTransformer = queryTransformer;
        this.queryExpander = queryExpander;
        this.queryArgumentPromptTemplate = queryArgumentPromptTemplate;
        this.webSearchRetriever = WebSearchRetriever.builder()
                .searchEngine(searchEngine)
                .dataCleaner(dataCleaner)
                .maxResults(2)
                .enableRanker(true)
                .documentRanker(documentRanker)
                .build();
    }

    /**
     * 搜索引擎检索 + 单轮问答
     * @param prompt
     * @return
     */
    @Override
    public String ask(String prompt, String chatId) {
        return deepseekChatClient.prompt()
                .advisors(createRetrievalAugmentationAdvisor())
                .advisors(x -> x
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE))
                .user(prompt)
                .call()
                .content();
    }

    /**
     * 搜索引擎检索 + 流式问答
     * @param prompt
     * @return
     */
    @Override
    public Flux<String> askStream(String prompt, String chatId) {
        return deepseekChatClient.prompt()
                .advisors(createRetrievalAugmentationAdvisor())
                .advisors(x -> x
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, CHAT_MEMORY_RETRIEVE_SIZE))
                .user(prompt)
                .stream()
                .content();
    }

    private RetrievalAugmentationAdvisor createRetrievalAugmentationAdvisor() {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(webSearchRetriever)
                .queryTransformers(queryTransformer)
                .queryAugmenter(
                        new CustomContextQueryAugmenter(
                                queryArgumentPromptTemplate,
                                null,
                                true)
                ).queryExpander(queryExpander)
                .documentJoiner(new ConcatenationDocumentJoiner())
                .build();
    }
}