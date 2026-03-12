package com.example.demo.pojo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款申请 + 订单摘要，便于列表展示「退的是什么」
 */
@Data
@Schema(description = "退款申请及订单摘要")
public class RefundWithOrderDTO {
    @Schema(description = "退款申请ID")
    private Long id;
    @Schema(description = "订单类型: 1-商品订单, 2-跑腿订单")
    private Integer orderType;
    @Schema(description = "订单ID")
    private Long orderId;
    @Schema(description = "订单号")
    private String orderNo;
    @Schema(description = "申请人ID")
    private Long applicantId;
    @Schema(description = "退款金额")
    private BigDecimal refundAmount;
    @Schema(description = "退款原因")
    private String reason;
    @Schema(description = "状态: 0-待处理, 1-已同意, 2-已退款, 3-已拒绝")
    private Integer status;
    @Schema(description = "处理人ID")
    private Long handlerId;
    @Schema(description = "处理备注")
    private String handleRemark;
    @Schema(description = "处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /** 订单摘要：商品订单为「商品名 x 数量, ...」，跑腿订单为标题 + 描述摘要 */
    @Schema(description = "订单内容摘要，便于识别退的是什么")
    private String orderSummary;
    /** 订单标题：商品订单为「商品订单」或首商品名，跑腿为跑腿标题 */
    @Schema(description = "订单标题/类型简述")
    private String orderTitle;
}
