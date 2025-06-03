package com.swimmingliu.rag;

import com.swimmingliu.model.entity.websearch.GenericSearchResult;
import com.swimmingliu.rag.core.IQSSearchEngine;
import com.swimmingliu.rag.data.DataClean;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.lang.Nullable;

import java.net.URISyntaxException;
import java.util.List;


/**
*   @author SwimmingLiu
*   @date 2025-06-03 16:19
*   @description: 网页搜索检索器
*/


public class WebSearchRetriever implements DocumentRetriever {

	private static final Logger logger = LoggerFactory.getLogger(WebSearchRetriever.class);

	private final IQSSearchEngine searchEngine;

	private final int maxResults;

	private final DataClean dataCleaner;

	private final DocumentPostProcessor documentPostProcessor;

	private final boolean enableRanker;

	private WebSearchRetriever(Builder builder) {

		this.searchEngine = builder.searchEngine;
		this.maxResults = builder.maxResults;
		this.dataCleaner = builder.dataCleaner;
		this.documentPostProcessor = builder.documentPostProcessor;
		this.enableRanker = builder.enableRanker;
	}

	@NotNull
	@Override
	public List<Document> retrieve(
			@Nullable Query query
	) {

		// 搜索
		GenericSearchResult searchResp = searchEngine.search(query.text());

		// 清洗数据
        List<Document> cleanerData = null;
        try {
            cleanerData = dataCleaner.getData(searchResp);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        logger.debug("cleaner data: {}", cleanerData);

		// 返回结果
		List<Document> documents = dataCleaner.limitResults(cleanerData, maxResults);

		logger.debug("WebSearchRetriever#retrieve() document size: {}, raw documents: {}",
				documents.size(),
				documents.stream().map(Document::getId).toArray()
		);

		return enableRanker ? ranking(query, documents) : documents;
	}

	private List<Document> ranking(Query query, List<Document> documents) {

		if (documents.size() == 1) {
			// 只有一个时，不需要 rank
			return documents;
		}

		try {

			List<Document> rankedDocuments = documentPostProcessor.process(query, documents);
			logger.debug("WebSearchRetriever#ranking() Ranked documents: {}", rankedDocuments.stream().map(Document::getId).toArray());
			return rankedDocuments;
		} catch (Exception e) {
			// 降级返回原始结果
			logger.error("ranking error", e);
			return documents;
		}
	}

	public static Builder builder() {
		return new Builder();
	}


	public static final class Builder {

		private IQSSearchEngine searchEngine;

		private int maxResults;

		private DataClean dataCleaner;

		private DocumentPostProcessor documentPostProcessor;

		// 默认开启 ranking
		private Boolean enableRanker = true;

		public Builder searchEngine(IQSSearchEngine searchEngine) {

			this.searchEngine = searchEngine;
			return this;
		}

		public Builder dataCleaner(DataClean dataCleaner) {

			this.dataCleaner = dataCleaner;
			return this;
		}

		public Builder maxResults(int maxResults) {

			this.maxResults = maxResults;
			return this;
		}

		public Builder documentRanker(DocumentPostProcessor documentPostProcessor) {
			this.documentPostProcessor = documentPostProcessor;
			return this;
		}

		public Builder enableRanker(Boolean enableRanker) {
			this.enableRanker = enableRanker;
			return this;
		}

		public WebSearchRetriever build() {

			return new WebSearchRetriever(this);
		}
	}

}
