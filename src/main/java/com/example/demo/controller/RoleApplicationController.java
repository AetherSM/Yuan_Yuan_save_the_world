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
@RequestMapping("/role-applications")
@Tag(name = "角色申请", description = "用户提交角色申请的接口")
public class RoleApplicationController {

    @Autowired
    private RoleApplicationService roleApplicationService;

    @PostMapping
    @Operation(summary = "提交角色申请", description = "当前登录用户提交角色变更申请")
    public Result<RoleApplication> apply(@RequestBody Map<String, String> body) {
        Long userId = BaseContext.getCurrentId();
        Integer targetType = body.get("targetType") == null ? null : Integer.valueOf(body.get("targetType"));
        String reason = body.get("reason");
        return Result.success(roleApplicationService.apply(userId, targetType, reason));
    }

    @GetMapping("/mine")
    @Operation(summary = "我的角色申请", description = "查看当前用户的角色申请记录")
    public Result<List<RoleApplication>> listMy() {
        Long userId = BaseContext.getCurrentId();
        return Result.success(roleApplicationService.listMy(userId));
    }
}

