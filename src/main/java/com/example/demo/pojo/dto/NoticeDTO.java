package com.example.demo.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "公告发布/更新信息")
public class NoticeDTO {
    @Schema(description = "公告ID(更新时传)")
    private Long noticeId;
    
    @Schema(description = "公告标题")
    private String title;
    
    @Schema(description = "公告内容")
    private String content;
    
    @Schema(description = "可见范围:0-所有人, 1-指定用户类型, 2-指定用户ID")
    private Integer visibility;
    
    @Schema(description = "目标用户类型, 逗号分隔")
    private String targetTypes;
    
    @Schema(description = "目标用户ID, JSON数组字符串")
    private String targetUsers;
}
