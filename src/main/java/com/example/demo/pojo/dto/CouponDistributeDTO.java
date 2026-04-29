package com.example.demo.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "优惠券定向发放请求")
public class CouponDistributeDTO {
    @Schema(description = "优惠券ID")
    private Long couponId;

    @Schema(description = "目标用户ID列表")
    private List<Long> userIds;
}
