package com.swimmingliu.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author swimmingliu
 */
@Data
@Component("RedisProperties")
@ConfigurationProperties(prefix = "memory.redis")
public class RedisProperties {
    private String host;
    private Integer port;
    private String password;
}
