package com.swimmingliu.controller;

import com.swimmingliu.common.enums.ChatTypeEnum;
import com.swimmingliu.common.response.Result;
import com.swimmingliu.service.ChatClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static com.swimmingliu.common.constants.MessageConstants.UNSUPPORTED_CHAT_TYPE;
import static com.swimmingliu.common.constants.PromptConstants.DEFAULT_QUESTION_PROMPT;
import static com.swimmingliu.common.utils.AIChatUtil.buildChatVO;
import static com.swimmingliu.common.utils.AIChatUtil.buildStreamResult;

/**
*   @author SwimmingLiu
*   @date 2025-06-03 17:40
*   @description: Deepseek 通用对话接口
*/


@RestController
@RequestMapping("/deepseek")
@Slf4j
@Tag(name = "Deepseek 通用对话接口", description = "DeepSeek Chat Model API")
public class DeepseekChatController extends BaseChatController{

    @Resource
    private ChatClientService deepseekChatClientService;

    @Resource
    private ChatClientService deepseekReasonClientService;

    @Resource
    private ChatClientService deepseekWebSearchClientService;

    @Resource
    private ChatClientService deepseekWebSearchReasonClientService;

    @GetMapping("/chat")
    @Operation(summary = "统一对话接口")
    public Result chat(
            @Parameter(description = "请输入您需要提问的问题", required = true)
            @NotNull @RequestParam(value = "question", defaultValue = DEFAULT_QUESTION_PROMPT) String question,
            @Parameter(description = "当前对话ID (首次请求可不填)") String chatId,
            @NotNull @Parameter(description = "对话类型", required = true) ChatTypeEnum chatType) {
        boolean isReason = chatType.equals(ChatTypeEnum.REASON) || chatType.equals(ChatTypeEnum.REASON_WEB);
        chatId = validateAndInitializeChatId(chatId, isReason, deepseekReasonClientService, deepseekChatClientService);

        String answer;
        boolean thinkStatus = false;

        switch (chatType) {
            case CHAT -> answer = deepseekChatClientService.ask(question, chatId);
            case REASON -> {
                answer = deepseekReasonClientService.ask(question, chatId);
                thinkStatus = true;
            }
            case CHAT_WEB -> answer = checkWebSearch(question) ?
                    deepseekWebSearchClientService.ask(question, chatId) :
                    deepseekChatClientService.ask(question, chatId);
            case REASON_WEB -> {
                answer = checkWebSearch(question) ?
                        deepseekWebSearchReasonClientService.ask(question, chatId) :
                        deepseekReasonClientService.ask(question, chatId);
                thinkStatus = true;
            }
            default -> throw new IllegalArgumentException(UNSUPPORTED_CHAT_TYPE);
        }
        return Result.ok().data(buildChatVO(chatId, answer, thinkStatus));
    }

    @GetMapping(value = "/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "统一对话接口-流式")
    public Flux<String> streamChat(
            @NotNull @Parameter(description = "请输入您需要提问的问题", required = true)
            @RequestParam(value = "question", defaultValue = DEFAULT_QUESTION_PROMPT) String question,
            @Parameter(description = "当前对话ID (首次请求可不填)") String chatId,
            @NotNull @Parameter(description = "对话类型", required = true) ChatTypeEnum chatType) {
        boolean isReason = chatType.equals(ChatTypeEnum.REASON) || chatType.equals(ChatTypeEnum.REASON_WEB);
        chatId = validateAndInitializeChatId(chatId, isReason, deepseekReasonClientService, deepseekChatClientService);

        Flux<String> stream;
        boolean thinkStatus = false;

        switch (chatType) {
            case CHAT -> stream = deepseekChatClientService.askStream(question, chatId);
            case REASON -> {
                stream = deepseekReasonClientService.askStream(question, chatId);
                thinkStatus = true;
            }
            case CHAT_WEB -> stream = checkWebSearch(question) ?
                    deepseekWebSearchClientService.askStream(question, chatId) :
                    deepseekChatClientService.askStream(question, chatId);
            case REASON_WEB -> {
                stream = checkWebSearch(question) ?
                        deepseekWebSearchReasonClientService.askStream(question, chatId) :
                        deepseekReasonClientService.askStream(question, chatId);
                thinkStatus = true;
            }
            default -> throw new IllegalArgumentException(UNSUPPORTED_CHAT_TYPE);
        }

        return buildStreamResult(stream, chatId, thinkStatus);
    }

    /**
     * 检查是否需要进行网络搜索
     * @param question
     * @return
     */
    private boolean checkWebSearch(String question) {
        return deepseekChatClientService.checkWebSearch(question);
    }
}