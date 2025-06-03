package com.swimmingliu.common.constants;

public interface RagConstants {
    /**
     * 元数据键值
     */
    String METADATA_QUERY = "query";
    String METADATA_TIME_RANGE = "timeRange";
    String METADATA_FILTERS = "filters";
    String METADATA_DETAIL = "detail";
    String METADATA_TYPE = "type";
    String METADATA_HOSTNAME = "hostname";
    String METADATA_HTML_SNIPPET = "htmlSnippet";
    String METADATA_TITLE = "title";
    String METADATA_MARKDOWN_TEXT = "markdownText";
    String METADATA_LINK = "link";
    String METADATA_SOURCE = "source";
    String METADATA_FILE_NAME = "file_name";
    String METADATA_KEY_PREFIX_SOURCE = "SOURCE:";
    String METADATA_KEY_PREFIX_FILE_NAME = "FILE_NAME:";

    /**
     * 正则表达式
     */
    String REGEX_HTML_TAGS = "<[^>]+>";
    String REGEX_WHITESPACE = "[\\n\\t\\r]+";
    String REGEX_INVISIBLE_CHARS = "[\\u200B-\\u200D\\uFEFF]";

    /**
     * 通用字符符号
     */

    String REPLACE_WHITESPACE = " ";
    String EMPTY_STRING = "";


    /**
     * 文档选择配置
     */
    int DEFAULT_TOTAL_DOCUMENTS = 10;
}