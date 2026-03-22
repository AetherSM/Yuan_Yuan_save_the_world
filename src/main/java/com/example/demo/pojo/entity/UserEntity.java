package com.example.demo.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户实体")
public class UserEntity {
    @Schema(description = "用户ID")
    private Long userId; // user_id
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "密码")
    private String password;
    
    @Schema(description = "昵称")
    private String nickname;
    
    @Schema(description = "头像URL")
    private String avatar;
    
    @Schema(description = "性别: 0-未知, 1-男, 2-女")
    private Integer gender; 
    
    @Schema(description = "学号")
    private String studentId;
    
    @Schema(description = "学校")
    private String school;
    
    @Schema(description = "宿舍地址")
    private String dormitory;
    
    @Schema(description = "余额")
    private BigDecimal balance;
    
    @Schema(description = "信用分")
    private Integer creditScore;
    
    @Schema(description = "用户类型: 0-管理员, 1-普通, 2-跑腿员, 3-商家")
    private Integer userType; 
    
    @Schema(description = "状态: 0-禁用, 1-正常")
    private Integer status; 
    
    @Schema(description = "注册时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
