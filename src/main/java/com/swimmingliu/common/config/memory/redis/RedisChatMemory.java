/*
 * Copyright 2024-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.swimmingliu.common.config.memory.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.swimmingliu.common.config.memory.redis.serializer.MessageDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;

public class RedisChatMemory implements ChatMemory, AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(RedisChatMemory.class);

	private static final String DEFAULT_KEY_PREFIX = "chat_memory:";

	private static final int DEFAULT_EXPIRY_SECONDS = 24 * 60 * 60;

	private final JedisPool jedisPool;

	private final Jedis jedis;

	private final ObjectMapper objectMapper;

	public RedisChatMemory(String host, int port, String password) {

		JedisPoolConfig poolConfig = new JedisPoolConfig();

		this.jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
		this.jedis = jedisPool.getResource();
		this.objectMapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addDeserializer(Message.class, new MessageDeserializer());
		this.objectMapper.registerModule(module);

		logger.info("Connected to Redis at {}:{}", host, port);
	}

	@Override
	public void add(String conversationId, List<Message> messages) {
		String key = DEFAULT_KEY_PREFIX + conversationId;

		for (Message message : messages) {
			try {
				String messageJson = objectMapper.writeValueAsString(message);
				jedis.rpush(key, messageJson);
				jedis.expire(key, DEFAULT_EXPIRY_SECONDS);
			} catch (JsonProcessingException e) {
				throw new RuntimeException("Error serializing message", e);
			}
		}
		logger.info("Added messages to conversationId: {} with expiry of {} seconds",
				conversationId, DEFAULT_EXPIRY_SECONDS);
	}

	@Override
	public List<Message> get(String conversationId, int lastN) {

		String key = DEFAULT_KEY_PREFIX + conversationId;

		List<String> messageStrings = jedis.lrange(key, -lastN, -1);
		List<Message> messages = new ArrayList<>();

		for (String messageString : messageStrings) {
			try {
				Message message = objectMapper.readValue(messageString, Message.class);
				messages.add(message);
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException("Error deserializing message", e);
			}
		}

		logger.info("Retrieved {} messages for conversationId: {}", messages.size(), conversationId);

		return messages;
	}

	@Override
	public void clear(String conversationId) {

		String key = DEFAULT_KEY_PREFIX + conversationId;

		jedis.del(key);
		logger.info("Cleared messages for conversationId: {}", conversationId);
	}

	@Override
	public void close() {

		if (jedis != null) {

			jedis.close();

			logger.info("Redis connection closed.");
		}
		if (jedisPool != null) {

			jedisPool.close();

			logger.info("Jedis pool closed.");
		}
	}

	public void clearOverLimit(String conversationId, int maxLimit, int deleteSize) {
		try {
			String key = DEFAULT_KEY_PREFIX + conversationId;
			List<String> all = jedis.lrange(key, 0, -1);

			if (all.size() >= maxLimit) {
				all = all.stream().skip(Math.max(0, deleteSize)).toList();
			}
			this.clear(conversationId);
			for (String message : all) {
				jedis.rpush(key, message);
			}
			jedis.expire(key, DEFAULT_EXPIRY_SECONDS);
		} catch (Exception e) {
			logger.error("Error clearing messages from Redis chat memory", e);
			throw new RuntimeException(e);
		}
	}

	public void updateMessageById(String conversationId, String messages) {
		String key = "spring_ai_alibaba_chat_memory:" + conversationId;
		try {
			this.jedis.del(key);
			this.jedis.rpush(key, new String[]{messages});
			// 设置过期时间
			this.jedis.expire(key, DEFAULT_EXPIRY_SECONDS);
		} catch (Exception e) {
			logger.error("Error updating messages from Redis chat memory", e);
			throw new RuntimeException(e);
		}
	}

}
