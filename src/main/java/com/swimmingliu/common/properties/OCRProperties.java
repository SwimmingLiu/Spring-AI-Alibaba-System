package com.swimmingliu.common.properties;

import lombok.Data;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "ocr")
public class OCRProperties {
    private AliCloud aliCloud = new AliCloud();
    private LlamaCloud llamaCloud = new LlamaCloud();

    @Data
    public static class AliCloud {
        private String accessKey;
        private String secretKey;
        private String endpoint;
    }

    @Data
    public static class LlamaCloud {
        private String accessKey;
    }
}