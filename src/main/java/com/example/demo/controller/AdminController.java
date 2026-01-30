package com.example.demo.controller;

import com.example.demo.pojo.entity.UserEntity;
import com.example.demo.pojo.result.Result;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/users")
@Tag(name = "管理员-用户管理", description = "管理员对用户进行管理的接口")
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "搜索用户", description = "根据条件搜索用户列表")
    public Result<List<UserEntity>> searchUsers(
            @Parameter(description = "手机号") @RequestParam(required = false) String phone,
            @Parameter(description = "昵称") @RequestParam(required = false) String nickname,
            @Parameter(description = "用户类型") @RequestParam(required = false) Integer userType,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        List<UserEntity> users = userService.searchUsers(phone, nickname, userType, status);
        return Result.success(users);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新用户状态", description = "启用或禁用指定用户")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        userService.updateUserStatus(id, payload.get("status"));
        return Result.success();
    }

    @PatchMapping("/{id}/type")
    @Operation(summary = "更新用户角色", description = "修改指定用户的角色")
    public Result<Void> updateUserType(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        userService.updateUserType(id, payload.get("userType"));
        return Result.success();
    }
}
