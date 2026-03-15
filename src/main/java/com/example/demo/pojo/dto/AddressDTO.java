package com.example.demo.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "地址传输对象")
public class AddressDTO {
    @Schema(description = "地址ID（更新时需要）")
    private Long addressId;
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
    @Schema(description = "是否默认地址：0-否，1-是")
    private Integer isDefault;
    @Schema(description = "经度")
    private java.math.BigDecimal longitude;
    @Schema(description = "纬度")
    private java.math.BigDecimal latitude;
}

