/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.swimmingliu.rag;

import com.swimmingliu.model.entity.websearch.GenericSearchResult;
import com.swimmingliu.rag.core.IQSSearchEngine;
import com.swimmingliu.rag.data.DataClean;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.ranking.DocumentRanker;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

public class WebSearchRetriever implements DocumentRetriever {

	private static final Logger logger = LoggerFactory.getLogger(WebSearchRetriever.class);

	private final IQSSearchEngine searchEngine;

	private final int maxResults;

	private final DataClean dataCleaner;

	private final DocumentRanker documentRanker;

	private final boolean enableRanker;

	private WebSearchRetriever(Builder builder) {

		this.searchEngine = builder.searchEngine;
		this.maxResults = builder.maxResults;
		this.dataCleaner = builder.dataCleaner;
		this.documentRanker = builder.documentRanker;
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
		List<Document> cleanerData = dataCleaner.getData(searchResp);
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

			List<Document> rankedDocuments = documentRanker.rank(query, documents);
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

		private DocumentRanker documentRanker;

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

		public Builder documentRanker(DocumentRanker documentRanker) {
			this.documentRanker = documentRanker;
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
