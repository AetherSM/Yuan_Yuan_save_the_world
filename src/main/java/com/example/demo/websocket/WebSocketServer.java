package com.example.demo.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务
 */
@Component
@Slf4j
@ServerEndpoint("/ws/{userId}")
public class WebSocketServer {

    /**
     * 存放所有在线客户端会话
     */
    private static final Map<Long, Session> sessionMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        log.info("WebSocket连接建立成功，用户ID: {}", userId);
        sessionMap.put(userId, session);
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        log.info("WebSocket连接关闭，用户ID: {}", userId);
        sessionMap.remove(userId);
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") Long userId) {
        log.info("收到来自用户 {} 的消息: {}", userId, message);
        // 这里如果是通过WebSocket发送聊天内容，可以在这里处理
        // 但目前我们的架构是 HTTP 发送消息，WebSocket 只负责实时推送到另一端
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误", error);
    }

    /**
     * 群发消息
     */
    public void sendToAllClient(String message) {
        for (Session session : sessionMap.values()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception e) {
                log.error("群发消息失败", e);
            }
        }
    }

    /**
     * 单发消息
     */
    public void sendToClient(Long userId, String message) {
        Session session = sessionMap.get(userId);
        if (session != null) {
            try {
                session.getBasicRemote().sendText(message);
                log.info("成功向用户 {} 推送实时消息", userId);
            } catch (Exception e) {
                log.error("向用户 {} 发送消息失败", userId, e);
            }
        } else {
            log.info("用户 {} 不在线，消息已存入数据库（通过HTTP接口已完成），不进行WebSocket实时推送", userId);
        }
    }
}
