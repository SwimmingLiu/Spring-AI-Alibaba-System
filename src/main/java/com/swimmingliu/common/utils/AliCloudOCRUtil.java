package com.swimmingliu.common.utils;

import com.aliyun.ocr_api20210707.Client;
import com.aliyun.ocr_api20210707.models.RecognizeAllTextRequest;
import com.aliyun.ocr_api20210707.models.RecognizeAllTextResponse;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static com.swimmingliu.common.constants.BaseConstants.DEFAULT_OCR_TYPE;

@Data
@AllArgsConstructor
@Slf4j
public class AliCloudOCRUtil {
    private static Client client;

    public AliCloudOCRUtil(String accessKey, String secretKey, String endpoint) throws Exception {
        client = createClient(accessKey, secretKey, endpoint);
    }

    private Client createClient(String accessKey, String secretKey, String endpoint) throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKey)
                .setAccessKeySecret(secretKey);
        config.endpoint = endpoint;
        return new Client(config);
    }

    /**
     * 通过URL进行OCR识别
     * @param imageUrl 图片URL
     * @param type 识别类型：general-通用文字, passport-护照, idCard-身份证, bankCard-银行卡,
     *            driving-驾驶证, vehicle-行驶证, invoice-发票等
     * @return OCR识别结果的JSON字符串
     */
    public String recognizeTextFromUrl(String imageUrl, String type) {
        try {
            RecognizeAllTextRequest request = new RecognizeAllTextRequest()
                    .setUrl(imageUrl)
                    .setType(type == null ? DEFAULT_OCR_TYPE : type);
            RuntimeOptions runtime = new RuntimeOptions();
            RecognizeAllTextResponse response = client.recognizeAllTextWithOptions(request, runtime);
            String jsonStr = JSON.toJSONString(response.getBody().getData());
            return JSON.parseObject(jsonStr).getString("content");
        } catch (Exception e) {
            log.error("OCR URL识别失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 使用默认类型(通用文字识别)进行OCR
     */
    public String recognizeTextFromUrl(String imageUrl) {
        return recognizeTextFromUrl(imageUrl, DEFAULT_OCR_TYPE);
    }
}