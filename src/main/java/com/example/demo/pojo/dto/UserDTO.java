package com.example.demo.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "用户注册/更新信息")
public class UserDTO {

    @Schema(description = "用户ID")
    private Long userId; // 用户ID（主键，插入后自动生成）

    @Schema(description = "手机号")
    private String phone; // 必传

    @Schema(description = "邮箱")
    private String email; // 必传（用于验证码）

    @Schema(description = "验证码")
    private String code; // 注册时必传

    @Schema(description = "密码")
    private String password; // 必传

    @Schema(description = "昵称")
    private String nickname; // 必传

    @Schema(description = "头像URL")
    private String avatar; // 可选（默认头像）

    @Schema(description = "性别: 0-未知, 1-男, 2-女")
    private Integer gender; // 可选，0/1/2

    @Schema(description = "学号")
    private String studentId; // 可选

    @Schema(description = "学校")
    private String school; // 可选

    @Schema(description = "宿舍地址")
    private String dormitory; // 可选

    @Schema(description = "余额")
    private BigDecimal balance; // 可选，默认 0

    @Schema(description = "信用分")
    private Integer creditScore; // 可选，默认 100

    @Schema(description = "用户类型: 1-普通, 2-跑腿员, 3-商家")
    private Integer userType; // 1-普通，2-跑腿员，3-商家

    @Schema(description = "状态: 0-禁用, 1-正常")
    private Integer status; // 0-禁用，1-正常

    @Schema(description = "创建时间")
    private LocalDateTime createTime; // 创建时间

    @Schema(description = "更新时间")
    private LocalDateTime updateTime; // 更新时间

}
