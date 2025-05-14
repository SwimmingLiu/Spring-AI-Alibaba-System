package com.swimmingliu.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author swimmingliu
 */
@Data
@Component("openAIDeepseekProperties")
@ConfigurationProperties(prefix = "ai.openai-deepseek")
public class OpenAIDeepseekProperties {
    private String url;
    private String apiKey;
    private String chatModelName;
    private String reasonModelName;
    private double temperature;
    private int maxTokens;
}
