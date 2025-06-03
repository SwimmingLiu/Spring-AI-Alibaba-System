package com.swimmingliu.controller;

import com.swimmingliu.common.constants.PromptConstants;
import com.swimmingliu.service.ChatClientService;
import lombok.extern.slf4j.Slf4j;

import static com.swimmingliu.common.utils.AIChatUtil.ensureChatId;
import static com.swimmingliu.common.utils.AIChatUtil.isChatIdValid;

/**
 * @author SwimmingLiu
 * @date 2025/6/3 17:49
 * @description: 基础AIChat类
 */

@Slf4j
public abstract class BaseChatController {


    protected void setSystemPrompt(String chatId, Boolean reason, ChatClientService reasonService, ChatClientService chatService) {
        if (reason){
            reasonService.ask(PromptConstants.DEFAULT_REASON_SETTING_PROMPT, chatId);
        } else {
            chatService.ask(PromptConstants.DEFAULT_SETTING_PROMPT, chatId);
        }
    }

    protected String validateAndInitializeChatId(String chatId, Boolean isReason,
            ChatClientService reasonService, ChatClientService chatService) {
        if (!isChatIdValid(chatId)) {
            chatId = ensureChatId(chatId);
            setSystemPrompt(chatId, isReason, reasonService, chatService);
        }
        return chatId;
    }
}