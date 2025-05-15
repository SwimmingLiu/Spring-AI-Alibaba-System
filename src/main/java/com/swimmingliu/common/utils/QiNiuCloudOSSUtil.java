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
    private static final String RESULT_FILE_DOMAIN = "https://oss.swimmingliu.cn/";

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
            return RESULT_FILE_DOMAIN + putRet.key;
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
            java.net.URL url = new java.net.URL(fileUrl);
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

            return new MultipartFile() {
                @Override
                public String getName() {
                    return fileName;
                }

                @Override
                public String getOriginalFilename() {
                    return fileName;
                }

                @Override
                public String getContentType() {
                    return null;
                }

                @Override
                public boolean isEmpty() {
                    return false;
                }

                @Override
                public long getSize() {
                    try {
                        return url.openConnection().getContentLength();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public byte[] getBytes() throws IOException {
                    try (InputStream is = url.openStream()) {
                        return is.readAllBytes();
                    }
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return url.openStream();
                }

                @Override
                public void transferTo(java.io.File dest) throws IOException {
                    try (InputStream is = url.openStream();
                         java.io.FileOutputStream fos = new java.io.FileOutputStream(dest)) {
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