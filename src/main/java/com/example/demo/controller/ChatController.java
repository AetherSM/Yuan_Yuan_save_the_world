package com.example.demo.controller;

import com.example.demo.pojo.entity.ChatMessage;
import com.example.demo.pojo.result.Result;
import com.example.demo.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天消息控制器
 */
@Tag(name = "聊天管理", description = "即时通讯消息发送与查询")
@RestController
@RequestMapping("/common/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 发送消息
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param content 消息内容
     * @param type 消息类型
     * @return 操作结果
     */
    @Operation(summary = "发送消息", description = "发送一条新的聊天消息")
    @PostMapping("/send")
    public Result<String> sendMessage(
            @Parameter(description = "发送者ID", required = true) @RequestParam Long senderId,
            @Parameter(description = "接收者ID", required = true) @RequestParam Long receiverId,
            @Parameter(description = "消息内容", required = true) @RequestParam String content,
            @Parameter(description = "消息类型: 1-文本, 2-图片") @RequestParam(defaultValue = "1") Integer type) {
        chatService.sendMessage(senderId, receiverId, content, type);
        return Result.success("发送成功");
    }

    /**
     * 获取历史消息
     * @param userId1 用户ID 1
     * @param userId2 用户ID 2
     * @return 聊天记录列表
     */
    @Operation(summary = "获取历史消息", description = "查询两个用户之间的历史聊天记录")
    @GetMapping("/history")
    public Result<List<ChatMessage>> getHistory(
            @Parameter(description = "用户ID 1", required = true) @RequestParam Long userId1,
            @Parameter(description = "用户ID 2", required = true) @RequestParam Long userId2) {
        return Result.success(chatService.getHistory(userId1, userId2));
    }

    /**
     * 标记消息已读
     * @param senderId 发送者ID (对方)
     * @param receiverId 接收者ID (自己)
     * @return 操作结果
     */
    @Operation(summary = "标记已读", description = "将对方发送给我的消息标记为已读")
    @PostMapping("/read")
    public Result<String> markRead(
            @Parameter(description = "发送者ID (对方)", required = true) @RequestParam Long senderId,
            @Parameter(description = "接收者ID (自己)", required = true) @RequestParam Long receiverId) {
        chatService.markRead(senderId, receiverId);
        return Result.success("已标记为已读");
    }

    /**
     * 获取商家的会话列表
     * @param merchantId 商家用户ID
     * @return 会话列表
     */
    @Operation(summary = "获取会话列表", description = "查询与商家有过沟通的用户列表")
    @GetMapping("/sessions")
    public Result<List<com.example.demo.pojo.vo.ChatSessionVO>> getSessions(
            @Parameter(description = "商家ID", required = true) @RequestParam Long merchantId) {
        return Result.success(chatService.getSessions(merchantId));
    }
}
