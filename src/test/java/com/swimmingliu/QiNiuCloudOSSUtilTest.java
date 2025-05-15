package com.swimmingliu;

import com.swimmingliu.common.utils.QiNiuCloudOSSUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class QiNiuCloudOSSUtilTest {

    @Resource
    private QiNiuCloudOSSUtil qiNiuCloudOSSUtil;
    private MockMultipartFile imageFile;
    private MockMultipartFile pdfFile;

    @BeforeEach
    void setUp() {
        // 准备测试文件
        imageFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "测试图片内容".getBytes()
        );

        pdfFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "测试PDF内容".getBytes()
        );
    }

    @Test
    void uploadFiles_WithImageFile_ShouldSucceed() throws Exception {
        String result = qiNiuCloudOSSUtil.uploadFiles(imageFile);
        assertNotNull(result);
        assertTrue(result.startsWith("https://oss.swimmingliu.cn/"));
        assertTrue(result.endsWith(".jpg"));
    }

    @Test
    void uploadFiles_WithPdfFile_ShouldSucceed() throws Exception {
        String result = qiNiuCloudOSSUtil.uploadFiles(pdfFile);
        assertNotNull(result);
        assertTrue(result.startsWith("https://oss.swimmingliu.cn/"));
        assertTrue(result.endsWith(".pdf"));
    }

    @Test
    void uploadFiles_WithEmptyFile_ShouldReturnEmptyString() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        String result = qiNiuCloudOSSUtil.uploadFiles(emptyFile);
        assertEquals("", result);
    }
}