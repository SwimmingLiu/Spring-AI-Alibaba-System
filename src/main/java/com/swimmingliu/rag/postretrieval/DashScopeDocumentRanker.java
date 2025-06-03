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

package com.swimmingliu.rag.postretrieval;

import com.alibaba.cloud.ai.dashscope.rerank.DashScopeRerankOptions;
import com.alibaba.cloud.ai.model.RerankModel;
import com.alibaba.cloud.ai.model.RerankRequest;
import com.alibaba.cloud.ai.model.RerankResponse;
import com.swimmingliu.common.exception.BizException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.postretrieval.document.DocumentPostProcessor;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

public class DashScopeDocumentRanker implements DocumentPostProcessor {

	private static final Logger logger = LoggerFactory.getLogger(DashScopeDocumentRanker.class);

	private final RerankModel rerankModel;

	public DashScopeDocumentRanker(RerankModel rerankModel) {
		this.rerankModel = rerankModel;
	}

	@NotNull
	@Override
	public List<Document> process(
			@Nullable Query query,
			@Nullable List<Document> documents
	) {

		if (documents == null || documents.isEmpty()) {
			return new ArrayList<>();
		}

		try {
			List<Document> reorderDocs = new ArrayList<>();

			// The caller controls the number of documents
			DashScopeRerankOptions rerankOptions = DashScopeRerankOptions.builder()
					.withTopN(documents.size())
					.build();

			if (Objects.nonNull(query) && StringUtils.hasText(query.text())) {
				// The assembly parameter calls rerankModel
				RerankRequest rerankRequest = new RerankRequest(
						query.text(),
						documents,
						rerankOptions
				);
				RerankResponse rerankResp = rerankModel.call(rerankRequest);

				rerankResp.getResults().forEach(res -> {
					Document outputDocs = res.getOutput();

					// Find and add to a new list
					Optional<Document> foundDocsOptional = documents.stream()
							.filter(doc ->
							{
								// debug rerank output.
								logger.debug("DashScopeDocumentRanker#rank() doc id: {}, outputDocs id: {}", doc.getId(), outputDocs.getId());
								return Objects.equals(doc.getId(), outputDocs.getId());
							})
							.findFirst();

					foundDocsOptional.ifPresent(reorderDocs::add);
				});
			}

			return reorderDocs;
		}
		catch (Exception e) {
			// Further processing is done depending on the type of exception
			throw new BizException(e.getMessage());
		}
	}
}
