package com.swimmingliu.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author swimmingliu
 */
@Data
@Component("MySQLProperties")
@ConfigurationProperties(prefix = "memory.mysql")
public class MySQLProperties {
    private String url;
    private String username;
    private String password;
}
