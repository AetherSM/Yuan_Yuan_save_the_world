package com.example.demo.controller;

import com.example.demo.context.BaseContext;
import com.example.demo.pojo.entity.RoleApplication;
import com.example.demo.pojo.result.Result;
import com.example.demo.service.RoleApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/role-applications")
@Tag(name = "管理员-角色申请管理", description = "管理员审批角色申请的接口")
public class AdminRoleApplicationController {

    @Autowired
    private RoleApplicationService roleApplicationService;

    @GetMapping
    @Operation(summary = "搜索角色申请", description = "按用户/目标角色/状态搜索角色申请")
    public Result<List<RoleApplication>> search(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "目标角色") @RequestParam(required = false) Integer targetType,
            @Parameter(description = "状态 0待审核 1通过 2拒绝") @RequestParam(required = false) Integer status
    ) {
        return Result.success(roleApplicationService.adminSearch(userId, targetType, status));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "审批通过", description = "管理员通过角色申请并修改用户角色")
    public Result<Void> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        Long handlerId = BaseContext.getCurrentId();
        String remark = body == null ? null : body.get("remark");
        roleApplicationService.approve(id, handlerId, remark);
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "审批拒绝", description = "管理员拒绝角色申请")
    public Result<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        Long handlerId = BaseContext.getCurrentId();
        String remark = body == null ? null : body.get("remark");
        roleApplicationService.reject(id, handlerId, remark);
        return Result.success();
    }
}

