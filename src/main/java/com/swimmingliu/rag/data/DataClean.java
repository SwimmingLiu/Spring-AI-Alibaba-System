package com.swimmingliu.rag.data;


import com.swimmingliu.common.constants.MessageConstants;
import com.swimmingliu.common.exception.BizException;
import com.swimmingliu.model.entity.websearch.GenericSearchResult;
import com.swimmingliu.model.entity.websearch.SceneItem;
import com.swimmingliu.model.entity.websearch.ScorePageItem;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static com.swimmingliu.common.constants.RagConstants.*;

/**
*   @author SwimmingLiu
*   @date 2025-06-03 16:21
*   @description: 数据清理类
*/


@Component
public class DataClean {
	/**
	 * 获取清理后的数据
	 * @param respData
	 * @return
	 * @throws URISyntaxException
	 */
    public List<Document> getData(GenericSearchResult respData) throws URISyntaxException {
        List<Document> documents = new ArrayList<>();
        Map<String, Object> metadata = getQueryMetadata(respData);
        // 处理场景类型 (时间、天气)
        if (!respData.getSceneItems().isEmpty()) {
            for (SceneItem sceneItem : respData.getSceneItems()) {
                Map<String, Object> sceneItemMetadata = getSceneItemMetadata(sceneItem);
                Document document = new Document.Builder()
                        .metadata(metadata)
                        .metadata(sceneItemMetadata)
                        .text(sceneItem.getDetail())
                        .build();
                documents.add(document);
            }
            return documents;
        }

        for (ScorePageItem pageItem : respData.getPageItems()) {
            Map<String, Object> pageItemMetadata = getPageItemMetadata(pageItem);
            Double score = getScore(pageItem);
            String text = getText(pageItem);

            if (Objects.equals(EMPTY_STRING, text)) {
                Media media = getMedia(pageItem);
                Document document = new Document.Builder()
                        .metadata(metadata)
                        .metadata(pageItemMetadata)
                        .media(media)
                        .score(score)
                        .build();
                documents.add(document);
                break;
            }

            Document document = new Document.Builder()
                    .metadata(metadata)
                    .metadata(pageItemMetadata)
                    .text(text)
                    .score(score)
                    .build();
            documents.add(document);
        }

        return documents;
    }

    private Double getScore(ScorePageItem pageItem) {
        return pageItem.getScore();
    }

    private Map<String, Object> getQueryMetadata(GenericSearchResult respData) {
        Map<String, Object> docsMetadata = new HashMap<>();

        if (Objects.nonNull(respData.getQueryContext())) {
            docsMetadata.put(METADATA_QUERY, respData.getQueryContext().getOriginalQuery().getQuery());

            if (Objects.nonNull(respData.getQueryContext().getOriginalQuery().getTimeRange())) {
                docsMetadata.put(METADATA_TIME_RANGE, respData.getQueryContext().getOriginalQuery().getTimeRange());
                docsMetadata.put(METADATA_FILTERS, respData.getQueryContext().getOriginalQuery().getTimeRange());
            }
        }

        return docsMetadata;
    }

    private Map<String, Object> getSceneItemMetadata(SceneItem sceneItem) {
        Map<String, Object> sceneItemMetadata = new HashMap<>();

        if (Objects.nonNull(sceneItem)) {
            if (Objects.nonNull(sceneItem.getDetail())) {
                sceneItemMetadata.put(METADATA_DETAIL, sceneItem.getDetail());
            }
            if (Objects.nonNull(sceneItem.getType())) {
                sceneItemMetadata.put(METADATA_TYPE, sceneItem.getType());
            }
        }

        return sceneItemMetadata;
    }

    private Map<String, Object> getPageItemMetadata(ScorePageItem pageItem) {
        Map<String, Object> pageItemMetadata = new HashMap<>();

        if (Objects.nonNull(pageItem)) {
            if (Objects.nonNull(pageItem.getHostname())) {
                pageItemMetadata.put(METADATA_HOSTNAME, pageItem.getHostname());
            }
            if (Objects.nonNull(pageItem.getHtmlSnippet())) {
                pageItemMetadata.put(METADATA_HTML_SNIPPET, pageItem.getHtmlSnippet());
            }
            if (Objects.nonNull(pageItem.getTitle())) {
                pageItemMetadata.put(METADATA_TITLE, pageItem.getTitle());
            }
            if (Objects.nonNull(pageItem.getMarkdownText())) {
                pageItemMetadata.put(METADATA_MARKDOWN_TEXT, pageItem.getMarkdownText());
            }
            if (Objects.nonNull(pageItem.getLink())) {
                pageItemMetadata.put(METADATA_LINK, pageItem.getLink());
            }
        }

        return pageItemMetadata;
    }

    private Media getMedia(ScorePageItem pageItem) throws URISyntaxException {
        String mime = pageItem.getMime();
        URL url;
        try {
            url = new URL(pageItem.getLink()).toURI().toURL();
        }
        catch (Exception e) {
            throw new BizException(MessageConstants.ERROR_INVALID_URL + pageItem.getLink());
        }
        return new Media(MimeType.valueOf(mime), url.toURI());
    }

    private String getText(ScorePageItem pageItem) {
        if (Objects.nonNull(pageItem.getMainText())) {
            String mainText = pageItem.getMainText();
            mainText = mainText.replaceAll(REGEX_HTML_TAGS, EMPTY_STRING);
            mainText = mainText.replaceAll(REGEX_WHITESPACE, REPLACE_WHITESPACE);
            mainText = mainText.replaceAll(REGEX_INVISIBLE_CHARS, EMPTY_STRING);
            return mainText.trim();
        }
        return EMPTY_STRING;
    }

    public List<Document> limitResults(List<Document> documents, int minResults) {
        return documents.subList(0, Math.min(documents.size(), minResults));
    }
}