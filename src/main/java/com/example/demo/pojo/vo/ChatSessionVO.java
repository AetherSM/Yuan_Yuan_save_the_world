package com.example.demo.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatSessionVO {
    private Long userId;        // 用户ID
    private String username;    // 用户名
    private String nickname;    // 昵称
    private String avatar;      // 头像
    private String lastMsg;     // 最后一条消息
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastTime; // 最后一条消息时间
    private Integer unreadCount;    // 未读数量
}
