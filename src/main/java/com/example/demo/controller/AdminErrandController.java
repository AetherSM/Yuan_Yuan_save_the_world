package com.example.demo.controller;

import com.example.demo.pojo.entity.ErrandOrder;
import com.example.demo.pojo.result.Result;
import com.example.demo.service.ErrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/errands")
@Tag(name = "管理员-跑腿任务管理", description = "管理员对跑腿任务进行管理的接口")
public class AdminErrandController {

    @Autowired
    private ErrandService errandService;

    @GetMapping
    @Operation(summary = "搜索跑腿任务", description = "根据条件搜索平台所有跑腿任务")
    public Result<List<ErrandOrder>> searchErrands(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "跑腿员ID") @RequestParam(required = false) Long runnerId,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status) {
        List<ErrandOrder> errands = errandService.searchErrands(userId, runnerId, status);
        return Result.success(errands);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新跑腿任务状态", description = "强制更新指定跑腿任务的状态")
    public Result<Void> updateErrandStatus(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        errandService.updateErrandStatusByAdmin(id, payload.get("status"));
        return Result.success();
    }
}
