package com.example.demo.pojo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "角色申请实体")
public class RoleApplication {

    @Schema(description = "申请ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "当前角色")
    private Integer currentType;

    @Schema(description = "申请目标角色")
    private Integer targetType;

    @Schema(description = "申请理由")
    private String reason;

    @Schema(description = "状态: 0-待审核, 1-通过, 2-拒绝")
    private Integer status;

    @Schema(description = "审核管理员ID")
    private Long handlerId;

    @Schema(description = "审核备注")
    private String handleRemark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

