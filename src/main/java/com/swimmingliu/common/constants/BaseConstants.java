package com.swimmingliu.common.constants;

public interface BaseConstants {
    Integer CONNECT_TIMEOUT_SECONDS = 30;
    Integer READ_TIMEOUT_SECONDS = 120;
    // 阿里云OCR通用识别类型
    String DEFAULT_OCR_TYPE = "Advanced";
    // AI对话上下文轮次大小
    Integer CHAT_MEMORY_RETRIEVE_SIZE = 100;
    // 流式输出空格处理
    String STREAM_OUTPUT_NORMAL_SPACE = " ";
    String STREAM_OUTPUT_SPECIAL_SPACE = "\u00A0";
}
