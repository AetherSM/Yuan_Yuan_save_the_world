package com.example.demo.service.impl;

import com.example.demo.mapper.ChatMessageMapper;
import com.example.demo.pojo.entity.ChatMessage;
import com.example.demo.pojo.vo.ChatSessionVO;
import com.example.demo.service.ChatService;
import com.example.demo.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private WebSocketServer webSocketServer;

    private static final String CHAT_SESSION_KEY = "chat:sessions:";

    @Override
    public void sendMessage(Long senderId, Long receiverId, String content, Integer type) {
        log.info("Sending message from {} to {}: {}", senderId, receiverId, content);
        ChatMessage msg = new ChatMessage();
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setMsgType(type);
        chatMessageMapper.insert(msg);

        // 1. 缓存失效
        redisTemplate.delete(CHAT_SESSION_KEY + receiverId);
        redisTemplate.delete(CHAT_SESSION_KEY + senderId);

        // 2. WebSocket 实时通知
        // 我们通知接收者有新消息
        // 为了让前端知道是新消息还是列表更新，我们可以发一个信号
        webSocketServer.sendToClient(receiverId, "NEW_MSG");
    }

    @Override
    public List<ChatMessage> getHistory(Long userId1, Long userId2) {
        log.info("Fetching history between {} and {}", userId1, userId2);
        return chatMessageMapper.listHistory(userId1, userId2);
    }

    @Override
    public void markRead(Long senderId, Long receiverId) {
        log.info("Marking messages from {} to {} as read", senderId, receiverId);
        chatMessageMapper.markRead(senderId, receiverId);
        // 清除自己的缓存以更新未读数
        redisTemplate.delete(CHAT_SESSION_KEY + receiverId);
    }

    @Override
    public List<ChatSessionVO> getSessions(Long merchantId) {
        String key = CHAT_SESSION_KEY + merchantId;
        
        // 1. 先尝试从 Redis 获取
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null && cached instanceof List) {
                log.info("商家 {} 会话列表命中 Redis 缓存", merchantId);
                return (List<ChatSessionVO>) cached;
            }
        } catch (Exception e) {
            log.error("Redis 读取异常: {}", e.getMessage());
        }

        // 2. Redis 没有，去 MySQL 查
        log.info("商家 {} 会话列表未命中 Redis，正在查询 MySQL...", merchantId);
        List<ChatSessionVO> sessions = chatMessageMapper.listSessions(merchantId);
        
        // 3. 查完回写 Redis (即使是空列表也存 1 分钟，防止缓存穿透)
        if (sessions == null) {
            sessions = new java.util.ArrayList<>();
        }
        
        try {
            long timeout = sessions.isEmpty() ? 1 : 5; // 空数据缓存1分钟，有数据缓存5分钟
            redisTemplate.opsForValue().set(key, sessions, timeout, TimeUnit.MINUTES);
            log.info("商家 {} 会话列表已同步至 Redis，记录数: {}", merchantId, sessions.size());
        } catch (Exception e) {
            log.error("Redis 写入异常: {}", e.getMessage());
        }
        
        return sessions;
    }
}
