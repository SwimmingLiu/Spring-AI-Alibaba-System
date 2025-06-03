package com.swimmingliu.common.config.redis;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swimmingliu.common.constants.RedisConstants;
import com.swimmingliu.mapper.ChatMessageMapper;
import com.swimmingliu.model.entity.ChatMessage;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedisBloomFilter {
    private static final String BLOOM_FILTER_KEY = RedisConstants.CHAT_MEMORY_KEY + "bloom:filter";
    private static final int MOD_VALUE = 1000000;
    // 使用多个哈希函数
    private static final int HASH_FUNCTIONS = 3;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @PostConstruct
    public void initializeFilter() {
        try {
            // 查询所有不重复的 conversationId
            LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(ChatMessage::getConversationId)
                    .groupBy(ChatMessage::getConversationId);

            List<ChatMessage> messages = chatMessageMapper.selectList(wrapper);
            for (ChatMessage message : messages) {
                add(message.getConversationId());
            }
        } catch (Exception e) {
            // 记录错误日志
        }
    }

    public boolean mightContain(String conversationId) {
        try {
            // 检查所有哈希函数的位
            for (int i = 0; i < HASH_FUNCTIONS; i++) {
                Boolean exists = redisTemplate.opsForValue()
                        .getBit(BLOOM_FILTER_KEY, calculateIndex(conversationId, i));
                if (exists == null || !exists) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            // 降级处理
            return true;
        }
    }

    public void add(String conversationId) {
        // 设置所有哈希函数的位
        for (int i = 0; i < HASH_FUNCTIONS; i++) {
            redisTemplate.opsForValue()
                    .setBit(BLOOM_FILTER_KEY, calculateIndex(conversationId, i), true);
        }
    }

    private long calculateIndex(String conversationId, int seed) {
        long hash = seed * 31L + conversationId.hashCode();
        return Math.abs(hash % MOD_VALUE);
    }
}