package com.swimmingliu.common.utils;

import com.alibaba.nacos.common.utils.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import org.apache.tika.Tika;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.swimmingliu.common.constants.RegexConstants.OSS_FILE_SOURCE;

@Component
@Slf4j
public class FileUtil {

    @Resource
    private AliCloudOCRUtil aliCloudOCRUtil;

    @Resource
    private QiNiuCloudOSSUtil qiNiuCloudOSSUtil;

    @Resource
    private LlamaParserOCRUtil llamaParserOCRUtil;

    private static final Tika tika = new Tika();

    /**
     * 使用Apache Tika判断URL是否为图片类型
     *
     * @param fileUrl 文件URL
     * @return true-图片类型，false-其他类型
     */
    private boolean isImageUrl(String fileUrl) {
        if (StringUtils.isEmpty(fileUrl)) {
            return false;
        }
        try {
            SSLUtil.trustAllHosts();
            Tika tika = new Tika();
            String mimeType = tika.detect(new URL(fileUrl));
            return mimeType != null && mimeType.startsWith("image/");
        } catch (Exception e) {
            log.error("文件类型判断失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断直接提取文档内容是否是空 (文档是图片的类型)
     *
     * @param documentText
     * @return
     */
    private boolean isEmptyContent(String documentText) {
        documentText = documentText.replaceAll(OSS_FILE_SOURCE, "").trim();
        return StringUtils.isEmpty(documentText);
    }

    /**
     * 获取文件中的文本内容
     *
     * @param fileUrl
     * @return
     */
    public String getDocumentText(String fileUrl) throws IOException, InterruptedException {
        String documentText;
        if (isImageUrl(fileUrl)) {
            documentText = getDocumentTextByOCR(fileUrl);
        } else {
            MultipartFile file = qiNiuCloudOSSUtil.getMultipartFileFromUrl(fileUrl);
            documentText = getDocumentTextByFile(file);
            if (isEmptyContent(documentText)) {
                documentText = llamaParserOCRUtil.parseFile(file);
            }
        }
        return Objects.isNull(documentText) ? "" : documentText;
    }

    /**
     * 获取文件中的文本内容
     *
     * @param file
     * @return
     */
    private String getDocumentTextByFile(MultipartFile file) {
        List<Document> documents = new TikaDocumentReader(file.getResource()).get();
        String documentText = documents.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n"));
        String content = documentText.replaceAll(OSS_FILE_SOURCE, "");
        if (StringUtils.isEmpty(content)) {
            return null;
        }
        return documentText;
    }

    /**
     * 通过OCR获取文件中的文本内容
     *
     * @param fileUrl 文件URL
     * @return 文本内容
     */
    private String getDocumentTextByOCR(String fileUrl) {
        String content = aliCloudOCRUtil.recognizeTextFromUrl(fileUrl);
        return "source: " + getFilenameFromUrl(fileUrl) + "\n\n\n" + content;
    }

    /**
     * 从URL当中获取文件名
     * @param fileUrl
     * @return
     */
    public String getFilenameFromUrl(String fileUrl) {
        if (StringUtils.isEmpty(fileUrl)) {
            return "";
        }
        try {
            return cn.hutool.core.io.FileUtil.getName(fileUrl);
        } catch (Exception e) {
            log.error("提取文件名失败: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 获取文件的MediaType
     *
     * @param file 待检测的文件
     * @return okhttp3的MediaType对象
     * @throws IOException 如果文件读取失败
     */
    public static MediaType getMediaType(MultipartFile file) throws IOException {
        String mimeType = tika.detect(file.getInputStream());
        return MediaType.parse(mimeType);
    }
}
