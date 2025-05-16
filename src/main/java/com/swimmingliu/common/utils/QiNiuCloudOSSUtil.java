package com.swimmingliu.common.utils;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.HttpsURLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Data
@AllArgsConstructor
@Slf4j
public class QiNiuCloudOSSUtil {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String prefixUrl;

    public String uploadFiles(MultipartFile file) throws Exception {
        Configuration cfg = new Configuration(Region.autoRegion());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
        UploadManager uploadManager = new UploadManager(cfg);

        String key = UUID.randomUUID() + getFileExtension(file.getOriginalFilename());
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);

        try (InputStream inputStream = file.getInputStream()) {
            Response response = uploadManager.put(inputStream, key, upToken, null, null);
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            return prefixUrl + putRet.key;
        } catch (QiniuException ex) {
            log.error("七牛云上传失败: {}", ex.response != null ? ex.response.toString() : "无响应");
        }
        return "";
    }

    private String getFileExtension(String fileName) {
        return fileName != null && fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
    }

    public MultipartFile getMultipartFileFromUrl(String fileUrl) {
        try {
            SSLUtil.trustAllHosts();
            java.net.URL url = new java.net.URL(fileUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            return new MultipartFile() {
                @Override
                public String getName() { return fileName; }
                @Override
                public String getOriginalFilename() { return fileName; }
                @Override
                public String getContentType() { return conn.getContentType(); }
                @Override
                public boolean isEmpty() { return false; }
                @Override
                public long getSize() { return conn.getContentLength(); }
                @Override
                public byte[] getBytes() throws IOException {
                    try (InputStream is = conn.getInputStream()) { return is.readAllBytes(); }
                }
                @Override
                public InputStream getInputStream() throws IOException { return conn.getInputStream(); }
                @Override
                public void transferTo(java.io.File dest) throws IOException {
                    try (InputStream is = conn.getInputStream();
                         FileOutputStream fos = new FileOutputStream(dest)) {
                        is.transferTo(fos);
                    }
                }
            };
        } catch (Exception e) {
            log.error("从URL获取MultipartFile失败: {}", e.getMessage());
            return null;
        }
    }
}