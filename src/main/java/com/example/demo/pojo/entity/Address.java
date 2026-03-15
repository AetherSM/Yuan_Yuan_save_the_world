package com.example.demo.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "收货地址实体")
public class Address {
    @Schema(description = "地址ID")
    private Long addressId;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "联系人姓名")
    private String contactName;
    @Schema(description = "联系电话")
    private String contactPhone;
    @Schema(description = "详细地址")
    private String address;
    @Schema(description = "楼号")
    private String building;
    @Schema(description = "门牌号")
    private String room;
    @Schema(description = "经度")
    private java.math.BigDecimal longitude;
    @Schema(description = "纬度")
    private java.math.BigDecimal latitude;
    @Schema(description = "是否默认地址：0-否，1-是")
    private Integer isDefault;
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

