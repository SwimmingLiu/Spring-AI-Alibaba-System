package com.swimmingliu.common.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.swimmingliu.common.constants.BaseConstants.CONNECT_TIMEOUT_SECONDS;
import static com.swimmingliu.common.constants.BaseConstants.READ_TIMEOUT_SECONDS;

@Slf4j
public class HttpUtil {
    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();

    public static JSONObject doRequest(Request request) throws IOException {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("请求失败: " + response.code());
            }
            return JSON.parseObject(response.body().string());
        }
    }

    public static Request.Builder createAuthRequest(String url, String apiKey) {
        return new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey);
    }
}