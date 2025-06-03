package com.swimmingliu.rag.core;


import com.swimmingliu.common.constants.WebSearchConstants;
import com.swimmingliu.common.exception.BizException;
import com.swimmingliu.common.properties.IQSSearchProperties;
import com.swimmingliu.model.entity.websearch.GenericSearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;

import java.util.Collections;

/**
 * @author swimmingliu
 */

@Slf4j
@Component
@EnableConfigurationProperties(IQSSearchProperties.class)
public class IQSSearchEngine {

    private final IQSSearchProperties iqsSearchProperties;
    private final RestClient restClient;

    public IQSSearchEngine(IQSSearchProperties iqsSearchProperties, RestClient.Builder restClientBuilder, ResponseErrorHandler responseErrorHandler) {
        this.iqsSearchProperties = iqsSearchProperties;
        this.restClient = restClientBuilder
                .baseUrl(WebSearchConstants.IQS_BASE_URL)
                .defaultHeaders(this::setHeaders)
                .defaultStatusHandler(responseErrorHandler)
                .build();
    }

    public GenericSearchResult search(String query) {
        ResponseEntity<GenericSearchResult> response = executeSearch(query);
        return parseResponse(response);
    }

    private ResponseEntity<GenericSearchResult> executeSearch(String query) {
        try {
            return restClient.get()
                    .uri("/search/genericSearch?query={query}&timeRange={timeRange}", query, WebSearchConstants.TIME_RANGE)
                    .retrieve()
                    .toEntity(GenericSearchResult.class);
        } catch (Exception e) {
            log.error("搜索请求失败: {}", e.getMessage(), e);
            throw new BizException("搜索服务暂时不可用，请稍后重试");
        }
    }

    private GenericSearchResult parseResponse(ResponseEntity<GenericSearchResult> response) {
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        }
        throw new BizException("搜索失败: " + response.getStatusCode().value());
    }

    private void setHeaders(HttpHeaders headers) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("user-agent", generateUserAgent());
        if (StringUtils.hasText(iqsSearchProperties.getApiKey())) {
            headers.set("X-API-Key", iqsSearchProperties.getApiKey());
        }
    }

    private static String generateUserAgent() {
        return String.format("SpringAiAlibabaPlayground/1.0.0; java/%s; platform/%s; processor/%s",
                System.getProperty("java.version"),
                System.getProperty("os.name"),
                System.getProperty("os.arch"));
    }
}