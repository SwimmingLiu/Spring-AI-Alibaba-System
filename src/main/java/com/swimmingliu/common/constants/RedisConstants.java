package com.swimmingliu.common.constants;

public interface RedisConstants {
    // 对话上下文存储key
    String CHAT_MEMORY_KEY = "ai:chat:memory:";
    // 默认存储2个小时
    Long CHAT_MEMORY_TTL = 60 * 60 * 2L;
}
