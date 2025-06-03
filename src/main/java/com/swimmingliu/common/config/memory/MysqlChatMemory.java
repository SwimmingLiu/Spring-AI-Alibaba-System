package com.swimmingliu.common.config.memory;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.swimmingliu.common.constants.BaseConstants;
import com.swimmingliu.mapper.ChatMessageMapper;
import com.swimmingliu.model.entity.ChatMessage;
import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
*   @author SwimmingLiu
*   @date 2025-06-03 16:45
*   @description: MySQL 存储上下文
*/

@Service("mysqlChatMemory")
public class MysqlChatMemory implements ChatMemory {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Value("classpath:db/check_table.sql")
    private Resource checkTableSql;

    @Value("classpath:db/create_table.sql")
    private Resource createTableSql;

    @Value("${memory.mysql.table-name}")
    private String tableName;

    @PostConstruct
    public void init() {
        checkAndCreateTable();
    }

    private String loadSqlFile(Resource resource) throws IOException {
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8)
                .replace("${tableName}", tableName);
    }

    private void checkAndCreateTable() {
        try {
            if (!chatMessageMapper.hasTable(loadSqlFile(checkTableSql))) {
                chatMessageMapper.createTable(loadSqlFile(createTableSql));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load SQL files", e);
        }
    }

    @Override
    public void add(@NotNull String conversationId, @NotNull Message message) {
        ChatMemory.super.add(conversationId, message);
    }

    @Override
    public void add(@NotNull String conversationId, @NotNull List<Message> messages) {
        addToMySQL(conversationId, messages);
    }

    @Async
    public void addToMySQL(@NotNull String conversationId, List<Message> messages) {
        messages.forEach(message -> {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setConversationId(conversationId);
            chatMessage.setMessages(message.getText());
            chatMessage.setType(message.getMessageType().name());
            chatMessageMapper.insert(chatMessage);
        });
    }

    @NotNull
    @Override
    public List<Message> get(@NotNull String conversationId) {
        // 此处如果CHAT_MEMORY_RETRIEVE_SIZE = -1表示提取所有的messages
        return get(conversationId, BaseConstants.CHAT_MEMORY_RETRIEVE_SIZE);
    }

    public List<Message> get(String conversationId, int lastN) {
        List<ChatMessage> chatMessages = chatMessageMapper.selectMessagesByConversationId(conversationId, lastN);
        return chatMessages.stream()
                .map(this::convertToMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void clear(@NotNull String conversationId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId);
        chatMessageMapper.delete(wrapper);
    }

    public void clearOverLimit(String conversationId, int maxLimit, int deleteSize) {
        long count = chatMessageMapper.selectCount(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getConversationId, conversationId)
        );

        if (count >= maxLimit) {
            chatMessageMapper.deleteOverLimit(conversationId, deleteSize);
        }
    }

    public Message convertToMessage(ChatMessage chatMessage) {
        MessageType type = MessageType.valueOf(chatMessage.getType());
        return switch (type) {
            case USER -> new UserMessage(chatMessage.getMessages());
            case ASSISTANT -> new AssistantMessage(chatMessage.getMessages());
            case SYSTEM -> new SystemMessage(chatMessage.getMessages());
            default -> null;
        };
    }
}
