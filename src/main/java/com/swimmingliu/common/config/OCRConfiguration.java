package com.swimmingliu.common.config;

import com.swimmingliu.common.properties.OCRProperties;
import com.swimmingliu.common.utils.AliCloudOCRUtil;
import com.swimmingliu.common.utils.LlamaParserOCRUtil;
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
        log.info("开始创建阿里云OCR工具类对象：{}",ocrProperties);
        return new AliCloudOCRUtil(ocrProperties.getAliCloud().getAccessKey(),
                ocrProperties.getAliCloud().getSecretKey(),
                ocrProperties.getAliCloud().getEndpoint());
    }

    @Bean
    @ConditionalOnMissingBean
    public LlamaParserOCRUtil llamaParserOCRUtil(OCRProperties ocrProperties) {
        log.info("开始创建LlmaParser工具类对象：{}", ocrProperties);
        return new LlamaParserOCRUtil(ocrProperties.getLlamaCloud().getAccessKey());
    }
}