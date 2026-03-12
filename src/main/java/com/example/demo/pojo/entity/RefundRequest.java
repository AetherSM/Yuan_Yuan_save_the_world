package com.example.demo.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "退款申请实体")
public class RefundRequest {
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
}
