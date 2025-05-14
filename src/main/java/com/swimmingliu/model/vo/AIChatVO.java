package com.swimmingliu.model.vo;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Schema(title = "AI对话VO", description = "AI对话返回数据模型")
@Data
@Builder
public class AIChatVO {
    @Schema(description = "对话id", example = "168523942123")
    private String chatId;

    @Schema(description = "LLM输出的答案", example = "这是一个AI助手的回答示例")
    private String answer;

    @Schema(description = "LLM思考过程（仅限于推理模型）", example = "1. 首先分析问题\n2. 然后...")
    private String think;

    @Schema(description = "是否具有思考过程", example = "true")
    private Boolean thinkStatus;
}
