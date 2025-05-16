package com.swimmingliu.common.constants;

public interface LlamaParserConstants {
    String LANGUAGE_PARAM = "language";
    // 文档语言为中文简体
    String LANGUAGE_CH_SIM = "ch_sim";
    // 文档语言为英语
    String LANGUAGE_EN = "en";
    String FILE_PARAM = "file";
    String STATUS_SUCCESS = "SUCCESS";
    String ID_FIELD = "id";
    String STATUS_FIELD = "status";
    String MARKDOWN_FIELD = "markdown";

    // API endpoints
    String BASE_URL = "https://api.cloud.llamaindex.ai/api/v1/parsing";
    String UPLOAD_ENDPOINT = BASE_URL + "/upload";
    String JOB_STATUS_ENDPOINT = BASE_URL + "/job/%s";
    String JOB_RESULT_ENDPOINT = BASE_URL + "/job/%s/result/markdown";

    // 轮询间隔时间（毫秒）
    long POLLING_INTERVAL = 500L;
}