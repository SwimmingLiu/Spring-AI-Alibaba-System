package com.swimmingliu.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.swimmingliu.common.response.Result;
import com.swimmingliu.model.vo.AIChatVO;
import com.swimmingliu.service.ChatClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import static com.swimmingliu.common.constants.BaseConstants.DEFAULT_QUESTION_PROMPT;
import static com.swimmingliu.common.utils.RandomUtil.generateChatId;

@RestController
@RequestMapping("/deepseek")
@Tag(name = "deepseek API", description = "DeepSeek Chat Model API")
public class DeepseekModelController {

    @Resource
    private ChatClientService deepseekChatClientService;

    @Resource
    private ChatClientService deepseekReasonClientService;

    @Resource
    private ChatClientService deepseekWebSearchClientService;

    @Resource
    private ChatClientService deepseekWebSearchReasonClientService;

    @GetMapping("/chat")
    @Operation(summary = "普通对话")
    public Result chat(
            @Parameter(description = "请输入您需要提问的问题", required = true)
            @RequestParam(value = "question", defaultValue = DEFAULT_QUESTION_PROMPT) String question,
            @Parameter(description = "当前对话ID") String chatId) {
        chatId = ensureChatId(chatId);
        String answer = deepseekChatClientService.ask(question, chatId);
        return Result.ok().data(buildChatVO(chatId, answer, false));
    }

    @GetMapping(value = "/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "普通对话-流式")
    public Flux<String> streamChat(
            @Parameter(description = "请输入您需要提问的问题", required = true)
            @RequestParam(value = "question", defaultValue = DEFAULT_QUESTION_PROMPT) String question,
            @Parameter(description = "当前对话ID") String chatId) {
        chatId = ensureChatId(chatId);
        return buildStreamResult(deepseekChatClientService.askStream(question, chatId), chatId, false);
    }

    @GetMapping("/reason/chat")
    @Operation(summary = "推理对话")
    public Result reasonChat(
            @Parameter(description = "请输入您需要提问的问题", required = true)
            @RequestParam(value = "question", defaultValue = DEFAULT_QUESTION_PROMPT) String question,
            @Parameter(description = "当前对话ID") String chatId) {
        chatId = ensureChatId(chatId);
        String answer = deepseekReasonClientService.ask(question, chatId);
        return Result.ok().data(buildChatVO(chatId, answer, true));
    }

    @GetMapping(value = "/stream/reason/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "推理对话-流式")
    public Flux<String> streamReasonChat(
            @Parameter(description = "请输入您需要提问的问题", required = true)
            @RequestParam(value = "question", defaultValue = DEFAULT_QUESTION_PROMPT) String question,
            @Parameter(description = "当前对话ID") String chatId) {
        chatId = ensureChatId(chatId);
        return buildStreamResult(deepseekReasonClientService.askStream(question, chatId), chatId, true);
    }

    @GetMapping("/search/chat")
    @Operation(summary = "普通对话-网络搜索")
    public Result searchChat(
            @Parameter(description = "请输入您需要提问的问题", required = true)
            @RequestParam(value = "question", defaultValue = DEFAULT_QUESTION_PROMPT) String question,
            @Parameter(description = "当前对话ID") String chatId) {
        chatId = ensureChatId(chatId);
        boolean needWebSearch = checkWebSearch(question);
        String answer;
        if (needWebSearch) {
            answer = deepseekWebSearchClientService.ask(question, chatId);
        } else {
            answer = deepseekChatClientService.ask(question, chatId);
        }
        return Result.ok().data(buildChatVO(chatId, answer, false));
    }

    @GetMapping(value = "/stream/search/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "普通对话-网络搜索-流式")
    public Flux<String> streamSearchChat(
            @Parameter(description = "请输入您需要提问的问题", required = true)
            @RequestParam(value = "question", defaultValue = DEFAULT_QUESTION_PROMPT) String question,
            @Parameter(description = "当前对话ID") String chatId) {
        chatId = ensureChatId(chatId);
        boolean needWebSearch = checkWebSearch(question);
        if (needWebSearch) {
            return buildStreamResult(deepseekWebSearchClientService.askStream(question, chatId), chatId, false);
        } else {
            return buildStreamResult(deepseekChatClientService.askStream(question, chatId), chatId, false);
        }
    }

    @GetMapping("/search/reason/chat")
    @Operation(summary = "推理对话-网络搜索")
    public Result searchReasonChat(
            @Parameter(description = "请输入您需要提问的问题", required = true)
            @RequestParam(value = "question", defaultValue = DEFAULT_QUESTION_PROMPT) String question,
            @Parameter(description = "当前对话ID") String chatId) {
        chatId = ensureChatId(chatId);
        boolean needWebSearch = checkWebSearch(question);
        String answer;
        if (needWebSearch) {
            answer = deepseekWebSearchReasonClientService.ask(question, chatId);
        } else {
            answer = deepseekReasonClientService.ask(question, chatId);
        }
        return Result.ok().data(buildChatVO(chatId, answer, false));
    }

    @GetMapping(value = "/stream/search/reason/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "推理对话-网络搜索-流式")
    public Flux<String> streamSearchReasonChat(
            @Parameter(description = "请输入您需要提问的问题", required = true)
            @RequestParam(value = "question", defaultValue = DEFAULT_QUESTION_PROMPT) String question,
            @Parameter(description = "当前对话ID") String chatId) {
        chatId = ensureChatId(chatId);
        boolean needWebSearch = checkWebSearch(question);
        if (needWebSearch) {
            return buildStreamResult(deepseekWebSearchReasonClientService.askStream(question, chatId), chatId, true);
        } else {
            return buildStreamResult(deepseekReasonClientService.askStream(question, chatId), chatId, false);
        }
    }

    /**
     * 构建非流式聊天返回对象
     */
    private AIChatVO buildChatVO(String chatId, String answer, boolean thinkStatus) {
        return AIChatVO.builder()
                .chatId(chatId)
                .answer(answer)
                .thinkStatus(thinkStatus)
                .build();
    }

    /**
     * 构建流式结果
     */
    private Flux<String> buildStreamResult(Flux<String> stream, String chatId, boolean thinkStatus) {
        return stream.map(text -> Result.ok()
                .data(AIChatVO.builder()
                        .chatId(chatId)
                        .answer(text)
                        .thinkStatus(thinkStatus)
                        .build())
                .toString());
    }

    /**
     * 确保chatId不为空
     */
    private String ensureChatId(String chatId) {
        return !StringUtils.hasText(chatId) ? String.valueOf(generateChatId()) : chatId;
    }

    /**
     * 检查是否需要调用网络搜索
     */
    private boolean checkWebSearch(String question) {
        return deepseekChatClientService.checkWebSearch(question);
    }
}