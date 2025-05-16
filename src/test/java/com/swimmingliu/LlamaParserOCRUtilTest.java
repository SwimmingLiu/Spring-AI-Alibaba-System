package com.swimmingliu;

import com.swimmingliu.common.utils.LlamaParserOCRUtil;
import com.swimmingliu.common.utils.QiNiuCloudOSSUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LlamaParserOCRUtilTest {

    @Resource
    private QiNiuCloudOSSUtil qiNiuCloudOSSUtil;
    @Resource
    private LlamaParserOCRUtil llamaParserOCRUtil;


    @Test
    void testParseOSSFile() {
        try {
            String testFileUrl = "";
            MultipartFile multipartFile = qiNiuCloudOSSUtil.getMultipartFileFromUrl(testFileUrl);

            assertNotNull(multipartFile, "从OSS获取的文件不能为空");
            assertFalse(multipartFile.isEmpty(), "文件内容不能为空");
            String parsedContent = llamaParserOCRUtil.parseFile(multipartFile);

            assertNotNull(parsedContent, "解析结果不能为空");
            assertFalse(parsedContent.trim().isEmpty(), "解析内容不能为空");
            System.out.println("解析结果: " + parsedContent);
        } catch (Exception e) {
            fail("测试失败: " + e.getMessage());
        }
    }
}