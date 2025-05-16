package com.swimmingliu.common.utils;

import com.alibaba.fastjson2.JSONObject;
import com.swimmingliu.common.constants.LlamaParserConstants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Data
@Slf4j
public class LlamaParserOCRUtil {
    private final String apiKey;

    public LlamaParserOCRUtil(String apiKey) {
        this.apiKey = apiKey;
    }

    public String parseFile(MultipartFile file) throws IOException, InterruptedException {
        if (file.isEmpty()) {
            throw new IOException("无法解析无效文件");
        }
        String jobId = uploadFile(file);
        while (!isParsingComplete(jobId)) {
            Thread.sleep(LlamaParserConstants.POLLING_INTERVAL);
        }
        return getParsingResult(jobId);
    }

    private String uploadFile(MultipartFile file) throws IOException {
        MediaType mediaType = FileUtil.getMediaType(file);
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(LlamaParserConstants.FILE_PARAM, file.getOriginalFilename(),
                        RequestBody.create(mediaType, file.getBytes()))
                .addFormDataPart(LlamaParserConstants.LANGUAGE_PARAM, LlamaParserConstants.LANGUAGE_CH_SIM)
                .build();

        Request request = HttpUtil.createAuthRequest(LlamaParserConstants.UPLOAD_ENDPOINT, apiKey)
                .post(requestBody)
                .build();
        return HttpUtil.doRequest(request).getString(LlamaParserConstants.ID_FIELD);
    }

    private boolean isParsingComplete(String jobId) throws IOException {
        Request request = HttpUtil.createAuthRequest(
                        String.format(LlamaParserConstants.JOB_STATUS_ENDPOINT, jobId), apiKey)
                .get()
                .build();
        JSONObject response = HttpUtil.doRequest(request);
        return LlamaParserConstants.STATUS_SUCCESS.equals(response.getString(LlamaParserConstants.STATUS_FIELD));
    }

    private String getParsingResult(String jobId) throws IOException {
        Request request = HttpUtil.createAuthRequest(
                        String.format(LlamaParserConstants.JOB_RESULT_ENDPOINT, jobId), apiKey)
                .get()
                .build();
        return HttpUtil.doRequest(request).getString(LlamaParserConstants.MARKDOWN_FIELD);
    }
}