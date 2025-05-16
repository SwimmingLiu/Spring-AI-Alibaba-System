package com.swimmingliu.controller;

import com.swimmingliu.common.enums.FileChatTypeEnum;
import com.swimmingliu.common.response.Result;
import com.swimmingliu.service.ChatClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static com.swimmingliu.common.constants.PromptConstants.DEFAULT_QUESTION_PROMPT;
import static com.swimmingliu.common.utils.AIChatUtil.*;

@RestController
@RequestMapping("/deepseek/file")
@Tag(name = "Deepseek文件对话接口", description = "DeepSeek File Chat Model API")
public class DeepseekFileController {

    @Resource
    private ChatClientService deepseekChatClientService;

    @Resource
    private ChatClientService deepseekReasonClientService;

    @GetMapping("/chat")
    @Operation(summary = "统一对话接口")
    public Result chat(
            @Parameter(description = "请输入您需要提问的问题", required = true)
            @RequestParam(value = "question", defaultValue = DEFAULT_QUESTION_PROMPT) String question,
            @Parameter(description = "当前对话ID (首次请求可不填)") String chatId,
            @Parameter(description = "对话类型", required = true) FileChatTypeEnum chatType,
            @Parameter(description = "附件URL", required = true) String fileUrl) {
        chatId = ensureChatId(chatId);
        String answer;
        boolean thinkStatus = false;
        switch (chatType) {
            case CHAT -> answer = deepseekChatClientService.askWithFile(question, chatId, fileUrl);
            case REASON -> {
                answer = deepseekReasonClientService.askWithFile(question, chatId, fileUrl);
                thinkStatus = true;
            }
            default -> throw new IllegalArgumentException("Unsupported chat type");
        }

        return Result.ok().data(buildChatVO(chatId, answer, thinkStatus));
    }

    @GetMapping(value = "/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "统一对话接口-流式")
    public Flux<String> streamChat(
            @Parameter(description = "请输入您需要提问的问题", required = true)
            @RequestParam(value = "question", defaultValue = DEFAULT_QUESTION_PROMPT) String question,
            @Parameter(description = "当前对话ID (首次请求可不填)") String chatId,
            @Parameter(description = "对话类型", required = true) FileChatTypeEnum chatType,
            @Parameter(description = "附件URL", required = true) String fileUrl) {
        chatId = ensureChatId(chatId);
        Flux<String> stream;
        boolean thinkStatus = false;
        switch (chatType) {
            case CHAT -> stream = deepseekChatClientService.askStreamWithFile(question, chatId, fileUrl);
            case REASON -> {
                stream = deepseekReasonClientService.askStreamWithFile(question, chatId, fileUrl);
                thinkStatus = true;
            }
            default -> throw new IllegalArgumentException("Unsupported chat type");
        }

        return buildStreamResult(stream, chatId, thinkStatus);
    }
}