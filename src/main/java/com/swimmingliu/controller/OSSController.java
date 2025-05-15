package com.swimmingliu.controller;

import com.swimmingliu.common.response.Result;
import com.swimmingliu.common.utils.QiNiuCloudOSSUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/oss")
@Tag(name = "文件上传接口", description = "File Upload API")
public class OSSController {

    @Resource
    private QiNiuCloudOSSUtil qiNiuCloudOSSUtil;

    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    public Result uploadFile(
            @Parameter(description = "上传的文件", required = true)
            @RequestPart("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error().message("上传文件不能为空");
            }
            String url = qiNiuCloudOSSUtil.uploadFiles(file);
            if (url.isEmpty()) {
                return Result.error().message("文件上传失败");
            }
            return Result.ok().data(url).message("文件上传成功");
        } catch (Exception e) {
            return Result.error().message("文件上传发生异常：" + e.getMessage());
        }
    }
}