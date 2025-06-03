package com.swimmingliu.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_chat_memory")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 会话ID
     */
    private String conversationId;
    /**
     * 消息内容
     */
    private String messages;
    /**
     * 消息类型
     */
    private String type;
}