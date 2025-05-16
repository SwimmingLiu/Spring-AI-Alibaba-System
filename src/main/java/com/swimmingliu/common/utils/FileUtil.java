package com.swimmingliu.common.utils;

import com.alibaba.nacos.common.utils.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import opennlp.tools.namefind.RegexNameFinderFactory;
import org.apache.tika.Tika;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.swimmingliu.common.constants.BaseConstants.DEFAULT_OCR_TYPE;
import static com.swimmingliu.common.constants.RegexConstants.OSS_FILE_SOURCE;

@Component
@Slf4j
public class FileUtil {

    @Resource
    private AliCloudOCRUtil aliCloudOCRUtil;

    @Resource
    private QiNiuCloudOSSUtil qiNiuCloudOSSUtil;

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
     * @param documentText
     * @return
     */
    private boolean isEmptyContent(String documentText){
        documentText = documentText.replaceAll(OSS_FILE_SOURCE,"").trim();
        return StringUtils.isEmpty(documentText);
    }

    /**
     * 获取文件中的文本内容
     *
     * @param fileUrl
     * @return
     */
    public String getDocumentText(String fileUrl) {
        String documentText;
        if (isImageUrl(fileUrl)) {
            documentText = getDocumentTextByOCR(fileUrl);
        } else {
            MultipartFile file = qiNiuCloudOSSUtil.getMultipartFileFromUrl(fileUrl);
            documentText = getDocumentTextByFile(file);
            if (isEmptyContent(documentText)) {
                // TODO 通过 LlamaParser 获取PDF等文档中的内容
                documentText = "";
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
     * @param fileUrl
     * @return
     */
    private String getDocumentTextByOCR(String fileUrl) {
        return aliCloudOCRUtil.recognizeTextFromUrl(fileUrl);
    }
}
