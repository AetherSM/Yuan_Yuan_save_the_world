package com.example.demo.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "商品实体")
public class Product {
    @Schema(description = "商品ID")
    private Long productId;
    @Schema(description = "商家ID")
    private Long sellerId;
    @Schema(description = "分类ID")
    private Integer categoryId;
    @Schema(description = "商品名称")
    private String productName;
    @Schema(description = "商品描述")
    private String description;
    @Schema(description = "价格")
    private BigDecimal price;
    @Schema(description = "原价")
    private BigDecimal originalPrice;
    @Schema(description = "库存")
    private Integer stock;
    @Schema(description = "销量")
    private Integer salesCount;
    @Schema(description = "主图URL")
    private String mainImage;
    @Schema(description = "详情图JSON")
    private String images; // JSON string
    @Schema(description = "发货地址")
    private String shippingAddress;
    @Schema(description = "状态: 0-下架 1-上架 2-待审核 3-审核拒绝")
    private Integer status; // 0-下架 1-上架 2-待审核 3-审核拒绝
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

