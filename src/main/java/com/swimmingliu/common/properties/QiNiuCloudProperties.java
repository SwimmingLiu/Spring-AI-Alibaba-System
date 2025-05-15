package com.swimmingliu.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oss.qiniuyun")
@Data
public class QiNiuCloudProperties {
    private String accessKey;
    private String secretKey;
    private String bucket;
}
