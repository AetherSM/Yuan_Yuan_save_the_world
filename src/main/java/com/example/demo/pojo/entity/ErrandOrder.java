package com.example.demo.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "跑腿订单实体")
public class ErrandOrder {
    @Schema(description = "订单ID")
    private Long orderId;
    
    @Schema(description = "订单编号")
    private String orderNo;
    
    @Schema(description = "下单用户ID")
    private Long userId;
    
    @Schema(description = "接单跑腿员ID")
    private Long runnerId;
    
    @Schema(description = "跑腿类型: 1-取快递, 2-送外卖, 3-代买, 4-其他")
    private Integer errandType; 
    
    @Schema(description = "订单标题")
    private String title;
    
    @Schema(description = "详细描述")
    private String description;
    
    @Schema(description = "取件地址")
    private String pickupAddress;
    
    @Schema(description = "送达地址")
    private String deliveryAddress;
    
    @Schema(description = "联系人姓名")
    private String contactName;
    
    @Schema(description = "联系电话")
    private String contactPhone;
    
    @Schema(description = "取件码")
    private String pickupCode;
    
    @Schema(description = "期望送达时间段")
    private String expectedTimeRange;
    
    @Schema(description = "赏金")
    private BigDecimal reward;
    
    @Schema(description = "小费")
    private BigDecimal tip;
    
    @Schema(description = "总金额")
    private BigDecimal totalAmount;
    
    @Schema(description = "图片URL列表 (JSON格式)")
    private String images; 
    
    @Schema(description = "截止时间/期望完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadline;
    
    @Schema(description = "订单状态: 0-待审核, 1-待接单, 2-已接单, 3-配送中, 4-已完成, 5-已取消, 6-审核拒绝, 7-退款中, 8-已退款")
    private Integer orderStatus; 
    
    @Schema(description = "取消原因")
    private String cancelReason;
    
    @Schema(description = "备注")
    private String remark;
    
    @Schema(description = "接单时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime acceptedAt;
    
    @Schema(description = "完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;
    
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
