package com.swimmingliu.common.config.memory;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swimmingliu.common.config.redis.RedisBloomFilter;
import com.swimmingliu.common.constants.BaseConstants;
import com.swimmingliu.common.constants.RedisConstants;
import com.swimmingliu.mapper.ChatMessageMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
*   @author SwimmingLiu
*   @date 2025-06-03 16:45
*   @description: Redis 存储上下文 (上下文存储方式采用 Redis (缓存) + MySQL(异步备份) 的方式)
*/

@Slf4j
@Service("redisChatMemory")
public class RedisChatMemory extends MysqlChatMemory {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private RedisBloomFilter bloomFilter;

    @Autowired
    private ObjectMapper objectMapper;

    @NotNull
    @Override
    public List<Message> get(@NotNull String conversationId) {
        List<String> cachedMessages = getFromRedis(conversationId);
        if (ObjectUtil.isEmpty(cachedMessages)) {
            if (!bloomFilter.mightContain(conversationId)) {
                return List.of();
            }
            List<Message> messages = chatMessageMapper
                    .selectMessagesByConversationId(conversationId, BaseConstants.CHAT_MEMORY_RETRIEVE_SIZE)
                    .stream()
                    .map(this::convertToMessage)
                    .collect(Collectors.toList());

            if (ObjectUtil.isNotEmpty(messages)) {
                saveToRedis(conversationId, messages);
                bloomFilter.add(conversationId);
            }
            return messages;
        }
        return deserializeMessages(cachedMessages);
    }

    @Override
    public void add(@NotNull String conversationId, @NotNull List<Message> messages) {
        saveToRedis(conversationId, messages);
        addToMySQL(conversationId, messages);
    }

    private List<String> getFromRedis(String conversationId) {
        String redisKey = RedisConstants.CHAT_MEMORY_KEY + conversationId;
        // 获取最新的CHAT_MEMORY_RETRIEVE_SIZE条上下文数据，默认为100条
        return redisTemplate.opsForList()
                .range(redisKey, -BaseConstants.CHAT_MEMORY_RETRIEVE_SIZE, -1);
    }

    private void saveToRedis(String conversationId, List<Message> messages) {
        String redisKey = RedisConstants.CHAT_MEMORY_KEY + conversationId;
        List<String> jsonMessages = messages.stream()
                .map(this::serializeMessage)
                .filter(ObjectUtil::isNotNull)
                .collect(Collectors.toList());
        redisTemplate.opsForList().rightPushAll(redisKey, jsonMessages);
        redisTemplate.expire(redisKey, RedisConstants.CHAT_MEMORY_TTL, TimeUnit.SECONDS);
    }

    private String serializeMessage(Message message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize message: {}", message, e);
            return null;
        }
    }

    private Message deserializeMessage(String jsonStr) {
        try {
            JsonNode node = objectMapper.readTree(jsonStr);
            String type = node.path("messageType").asText("USER");
            String content = node.path("text").asText();
            return switch (type) {
                case "USER" -> new UserMessage(content);
                case "ASSISTANT" -> new AssistantMessage(content);
                case "SYSTEM" -> new SystemMessage(content);
                default -> throw new IllegalStateException("Unexpected value: " + type);
            };
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize message: {}", jsonStr, e);
            return null;
        }
    }

    private List<Message> deserializeMessages(List<String> jsonMessages) {
        return jsonMessages.stream()
                .map(this::deserializeMessage)
                .filter(ObjectUtil::isNotNull)
                .collect(Collectors.toList());
    }

    @Override
    public void clear(@NotNull String conversationId) {
        clearRedisCache(conversationId);
    }

    private void clearRedisCache(String conversationId) {
        String redisKey = RedisConstants.CHAT_MEMORY_KEY + conversationId;
        redisTemplate.delete(redisKey);
    }
}
