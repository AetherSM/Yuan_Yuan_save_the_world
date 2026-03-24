package com.example.demo.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoticeEntity {
    private Long noticeId;
    private String title;
    private String content;
    private Integer visibility; // 0-所有人, 1-指定用户类型, 2-指定用户ID
    private String targetTypes; // 逗号分隔, 如 "1,2"
    private String targetUsers; // JSON数组字符串, 如 "[1,2,3]"
    private Long adminId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
