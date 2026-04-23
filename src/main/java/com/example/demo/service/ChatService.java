package com.example.demo.service;

import com.example.demo.pojo.entity.ChatMessage;
import com.example.demo.pojo.vo.ChatSessionVO;
import java.util.List;

/**
 * 聊天业务接口
 */
public interface ChatService {
    
    /**
     * 发送消息
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param content 消息内容
     * @param type 消息类型
     */
    void sendMessage(Long senderId, Long receiverId, String content, Integer type);
    
    /**
     * 获取历史消息
     * @param userId1 用户ID 1
     * @param userId2 用户ID 2
     * @return 聊天记录
     */
    List<ChatMessage> getHistory(Long userId1, Long userId2);
    
    /**
     * 标记消息已读
     * @param senderId 发送者ID (对方)
     * @param receiverId 接收者ID (自己)
     */
    void markRead(Long senderId, Long receiverId);

    /**
     * 获取商家的会话列表
     * @param merchantId 商家用户ID
     * @return 会话列表
     */
    List<ChatSessionVO> getSessions(Long merchantId);
}
