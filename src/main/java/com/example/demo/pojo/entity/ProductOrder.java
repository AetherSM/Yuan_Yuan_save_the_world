package com.example.demo.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "商品订单实体")
public class ProductOrder {
    @Schema(description = "订单ID")
    private Long orderId;
    @Schema(description = "订单号")
    private String orderNo;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "商家ID")
    private Long sellerId;
    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;
    @Schema(description = "收货地址")
    private String deliveryAddress;
    @Schema(description = "联系人姓名")
    private String contactName;
    @Schema(description = "联系电话")
    private String contactPhone;
    @Schema(description = "订单状态: 1-待支付, 2-待发货, 3-待收货, 4-已完成, 5-已取消, 6-退款中, 7-已退款")
    private Integer orderStatus; // 1-5 同上, 6-退款中, 7-已退款
    @Schema(description = "支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;
    @Schema(description = "发货时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime shipTime;
    @Schema(description = "确认收货时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime confirmTime;
    @Schema(description = "取消原因")
    private String cancelReason;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "订单商品摘要（展示用，如：可乐 x2；面包 x1）")
    private String itemSummary;
}

