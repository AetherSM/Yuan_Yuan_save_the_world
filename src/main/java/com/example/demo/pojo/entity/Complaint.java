package com.example.demo.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "投诉信息实体")
public class Complaint {
    @Schema(description = "投诉ID")
    private Long complaintId;
    
    @Schema(description = "关联订单ID")
    private Long orderId;
    
    @Schema(description = "订单类型: 1-商品订单, 2-跑腿订单")
    private Integer orderType; 
    
    @Schema(description = "投诉人ID")
    private Long complainantId;
    
    @Schema(description = "投诉原因/内容")
    private String reason;
    
    @Schema(description = "状态: 0-待处理, 1-处理中, 2-已完成, 3-已拒绝")
    private Integer status; 
    
    @Schema(description = "处理结果")
    private String result;
    
    @Schema(description = "提交时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
