package com.swimmingliu.common.config;

import com.swimmingliu.common.config.memory.mysql.MySQLChatMemory;
import com.swimmingliu.common.config.memory.redis.RedisChatMemory;
import com.swimmingliu.common.properties.MySQLProperties;
import com.swimmingliu.common.properties.RedisProperties;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ChatMemoryConfig {

	@Resource
	private MySQLProperties mySQLProperties;

	@Resource
	private RedisProperties redisProperties;

	@Bean("mysqlChatMemory")
	public ChatMemory mysqlChatMemory() {
		return new MySQLChatMemory(
				mySQLProperties.getUsername(),
				mySQLProperties.getPassword(),
				mySQLProperties.getUrl()
		);
	}

	@Bean("redisChatMemory")
	public ChatMemory redisChatMemory() {
		return new RedisChatMemory(
				redisProperties.getHost(),
				redisProperties.getPort(),
				redisProperties.getPassword()
		);
	}
}
