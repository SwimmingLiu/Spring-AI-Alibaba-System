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

import java.io.InputStream;
import java.util.UUID;

@Data
@AllArgsConstructor
@Slf4j
public class QiNiuCloudOSSUtil {
    private String accessKey;
    private String secretKey;
    private String bucket;

    private static String resultfileDomain = "https://oss.swimmingliu.cn/";

    public String uploadFiles(MultipartFile file) throws Exception {

        //构造一个带指定Region对象的配置类
        Configuration cfg = new Configuration(Region.autoRegion());
        // 指定分片上传版本
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;
        UploadManager uploadManager = new UploadManager(cfg);

        // 获取文件的数据流
        InputStream inputStream = file.getInputStream();
        String originFileName = file.getOriginalFilename();

        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = null;
        if (originFileName != null) {
            key = UUID.randomUUID() + originFileName.substring(originFileName.lastIndexOf("."));
        }
        Auth auth = Auth.create(accessKey, secretKey);
        String upToken = auth.uploadToken(bucket);
        try {
            Response response = uploadManager.put(inputStream, key, upToken,null,null);
            //解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            return resultfileDomain + putRet.key;

        } catch (QiniuException ex) {
            if (ex.response != null) {
                try {
                    String body = ex.response.toString();
                    log.info("七牛云上传失败: {}", body);
                } catch (Exception ignored) {

                }
            }
        }
        return "";
    }

}
