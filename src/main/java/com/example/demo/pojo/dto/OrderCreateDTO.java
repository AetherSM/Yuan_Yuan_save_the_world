package com.example.demo.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "订单创建信息")
public class OrderCreateDTO {
    @Schema(description = "收货地址ID")
    private Long addressId;
    @Schema(description = "收货地址详情（冗余）")
    private String deliveryAddress;
    @Schema(description = "联系人姓名（冗余）")
    private String contactName;
    @Schema(description = "联系电话（冗余）")
    private String contactPhone;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "用户优惠券ID")
    private Long couponId;
    @Schema(description = "订单商品项列表")
    private List<OrderItemDTO> items;
}
