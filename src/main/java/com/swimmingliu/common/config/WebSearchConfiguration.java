package com.swimmingliu.common.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.model.RerankModel;
import com.swimmingliu.common.properties.DashscopeDeepseekProperties;
import com.swimmingliu.rag.postretrieval.DashScopeDocumentRanker;
import com.swimmingliu.rag.preretrieval.query.expansion.MultiQueryExpander;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@Configuration
public class WebSearchConfiguration {

	@Resource
	public DashscopeDeepseekProperties dashscopeDeepseekProperties;

    @Bean
    public DashScopeDocumentRanker dashScopeDocumentRanker(
            RerankModel rerankModel
    ) {
        return new DashScopeDocumentRanker(rerankModel);
    }

    @Bean
    public QueryTransformer queryTransformer(
            @Qualifier("dashscopeChatModel") ChatModel chatModel,
            @Qualifier("transformerPromptTemplate") PromptTemplate transformerPromptTemplate
    ) {
        DashScopeChatOptions option = DashScopeChatOptions.builder()
                .withModel(dashscopeDeepseekProperties.getChatModelName())
                .build();
        ChatClient chatClient = ChatClient.builder(chatModel).defaultOptions(option).build();
        return RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClient.mutate())
                .promptTemplate(transformerPromptTemplate)
                .targetSearchSystem("Web Search")
                .build();
    }

    @Bean
    public QueryExpander queryExpander(
            @Qualifier("dashscopeChatModel") ChatModel chatModel
    ) {
        DashScopeChatOptions option = DashScopeChatOptions.builder()
                .withModel(dashscopeDeepseekProperties.getChatModelName())
                .build();
        ChatClient chatClient = ChatClient.builder(chatModel).defaultOptions(option).build();
        return MultiQueryExpander.builder()
                .chatClientBuilder(chatClient.mutate())
                .numberOfQueries(2)
                .build();
    }

}
