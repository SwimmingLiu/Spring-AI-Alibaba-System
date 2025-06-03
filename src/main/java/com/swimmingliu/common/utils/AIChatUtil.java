package com.swimmingliu.common.utils;

import com.alibaba.nacos.common.utils.StringUtils;
import com.swimmingliu.common.response.Result;
import com.swimmingliu.model.vo.AIChatVO;
import reactor.core.publisher.Flux;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.swimmingliu.common.constants.BaseConstants.STREAM_OUTPUT_NORMAL_SPACE;
import static com.swimmingliu.common.constants.BaseConstants.STREAM_OUTPUT_SPECIAL_SPACE;
import static com.swimmingliu.common.utils.RandomUtil.generateChatId;

public class AIChatUtil {
    /**
     * 提取思考内容并处理答案
     */
    private static void processThinkContent(AIChatVO.AIChatVOBuilder builder, String answer, boolean thinkStatus) {
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
    public static AIChatVO buildChatVO(String chatId, String answer, boolean thinkStatus) {
        AIChatVO.AIChatVOBuilder builder = AIChatVO.builder()
                .chatId(chatId)
                .thinkStatus(thinkStatus);
        processThinkContent(builder, answer, thinkStatus);
        return builder.build();
    }

    /**
     * 构建流式结果
     */
    public static Flux<String> buildStreamResult(Flux<String> stream, String chatId, boolean thinkStatus) {
        return stream.map(text -> {
            // 将普通空格替换为不间断空格
            String processedText = text.replace(STREAM_OUTPUT_NORMAL_SPACE, STREAM_OUTPUT_SPECIAL_SPACE);
            AIChatVO.AIChatVOBuilder builder = AIChatVO.builder()
                    .chatId(chatId)
                    .thinkStatus(thinkStatus);
            processThinkContent(builder, processedText, thinkStatus);
            return Result.ok()
                    .data(builder.build())
                    .toString();
        });
    }

    /**
     * 确保chatId不为空
     */
    public static String ensureChatId(String chatId) {
        return  !isChatIdValid(chatId) ? String.valueOf(generateChatId()) : chatId;
    }

    /**
     * 检查chatId是否有效
     * @param chatId
     * @return
     */
    public static Boolean isChatIdValid(String chatId) {
        return StringUtils.isNotBlank(chatId) && chatId.matches("\\d+");
    }
}
