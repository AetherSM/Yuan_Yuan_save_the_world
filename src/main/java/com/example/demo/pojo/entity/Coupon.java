package com.example.demo.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "优惠券实体")
public class Coupon {
    @Schema(description = "优惠券ID")
    private Long couponId;
    
    @Schema(description = "优惠券名称")
    private String name;
    
    @Schema(description = "类型: 1-满减, 2-折扣")
    private Integer type; 
    
    @Schema(description = "面值/折扣率")
    private BigDecimal value;
    
    @Schema(description = "最低消费金额")
    private BigDecimal minSpend;
    
    @Schema(description = "发行总量，null或0表示不限制")
    private Integer totalCount;
    
    @Schema(description = "已领取数量")
    private Integer receivedCount;
    
    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    @Schema(description = "状态: 0-无效, 1-有效")
    private Integer status; 

    @Schema(description = "发券方类型: 0-管理员, 1-商家")
    private Integer issuerType;

    @Schema(description = "发券方ID(管理员ID或商家ID)")
    private Long issuerId;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
