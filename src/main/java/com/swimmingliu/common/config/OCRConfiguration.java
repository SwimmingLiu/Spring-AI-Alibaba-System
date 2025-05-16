package com.swimmingliu.common.config;

import com.swimmingliu.common.properties.OCRProperties;
import com.swimmingliu.common.utils.AliCloudOCRUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OCRConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AliCloudOCRUtil aliCloudOCRUtil(OCRProperties ocrProperties) throws Exception {
        log.info("开始创建七牛云文件上传工具类对象：{}",ocrProperties);
        return new AliCloudOCRUtil(ocrProperties.getAccessKey(),
                ocrProperties.getSecretKey(),
                ocrProperties.getEndpoint());
    }
}