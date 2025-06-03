package com.swimmingliu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swimmingliu.model.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    Boolean hasTable(@Param("sql") String sql);

    void createTable(@Param("sql") String sql);

    // 根据会话ID查询消息记录
    List<ChatMessage> selectMessagesByConversationId(@Param("conversationId") String conversationId,
                                                     @Param("lastN") int lastN);

    // 删除超出限制的消息
    void deleteOverLimit(@Param("conversationId") String conversationId,
                         @Param("deleteSize") int deleteSize);
}