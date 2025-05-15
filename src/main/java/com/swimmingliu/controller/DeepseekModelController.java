package com.swimmingliu.controller;

import com.alibaba.nacos.common.utils.StringUtils;
import com.swimmingliu.common.enums.ChatTypeEnum;
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

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static com.swimmingliu.common.constants.BaseConstants.DEFAULT_QUESTION_PROMPT;
import static com.swimmingliu.common.utils.RandomUtil.generateChatId;

@RestController
@RequestMapping("/deepseek")
@Tag(name = "Deepseek API", description = "DeepSeek Chat Model API")
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
    @Operation(summary = "统一对话接口")
    public Result chat(
            @Parameter(description = "请输入您需要提问的问题", required = true)
            @RequestParam(value = "question", defaultValue = DEFAULT_QUESTION_PROMPT) String question,
            @Parameter(description = "当前对话ID (首次请求可不填)") String chatId,
            @Parameter(description = "对话类型", required = true) ChatTypeEnum chatType) {
        chatId = ensureChatId(chatId);
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
            @Parameter(description = "对话类型", required = true) ChatTypeEnum chatType) {
        chatId = ensureChatId(chatId);
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
            default -> throw new IllegalArgumentException("Unsupported chat type");
        }

        return buildStreamResult(stream, chatId, thinkStatus);
    }

    /**
     * 提取思考内容并处理答案
     */
    private void processThinkContent(AIChatVO.AIChatVOBuilder builder, String answer, boolean thinkStatus) {
        if (!thinkStatus) {
            builder.answer(answer);
            builder.think(null);
            return;
        }
        // 分离思维链和答案
        String think = null;
        String processedAnswer = answer;
        Pattern pattern = Pattern.compile("<think>(.*?)</think>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(answer);
        if (matcher.find()) {
            think = matcher.group(1).trim();
            int start = answer.indexOf("</think>");
            processedAnswer = answer.substring(start + 8).trim();
        }
        builder.answer(processedAnswer);
        builder.think(think);
    }

    /**
     * 构建非流式聊天返回对象
     */
    private AIChatVO buildChatVO(String chatId, String answer, boolean thinkStatus) {
        AIChatVO.AIChatVOBuilder builder = AIChatVO.builder()
                .chatId(chatId)
                .thinkStatus(thinkStatus);

        processThinkContent(builder, answer, thinkStatus);
        return builder.build();
    }

    /**
     * 构建流式结果
     */
    private Flux<String> buildStreamResult(Flux<String> stream, String chatId, boolean thinkStatus) {
        return stream.map(text -> {
            AIChatVO.AIChatVOBuilder builder = AIChatVO.builder()
                    .chatId(chatId)
                    .thinkStatus(thinkStatus);

            processThinkContent(builder, text, thinkStatus);
            return Result.ok()
                    .data(builder.build())
                    .toString();
        });
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