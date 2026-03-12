package com.example.demo.controller;

import com.example.demo.context.BaseContext;
import com.example.demo.pojo.dto.RefundWithOrderDTO;
import com.example.demo.pojo.entity.RefundRequest;
import com.example.demo.pojo.result.Result;
import com.example.demo.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员-退款管理：查看全部退款、同意/拒绝
 */
@RestController
@RequestMapping("/admin/refunds")
@Tag(name = "管理员-退款管理", description = "管理员查看与处理全平台退款申请")
public class AdminRefundController {

    @Autowired
    private RefundService refundService;

    @GetMapping
    @Operation(summary = "退款列表", description = "按订单类型、状态筛选")
    public Result<List<RefundWithOrderDTO>> list(
            @Parameter(description = "订单类型: 1-商品 2-跑腿") @RequestParam(required = false) Integer orderType,
            @Parameter(description = "状态: 0-待处理 2-已退款 3-已拒绝") @RequestParam(required = false) Integer status) {
        return Result.success(refundService.listAll(orderType, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "退款详情")
    public Result<RefundRequest> getById(@PathVariable Long id) {
        RefundRequest req = refundService.getById(id);
        if (req == null) {
            return Result.error("退款申请不存在");
        }
        return Result.success(req);
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "同意退款", description = "管理员同意并执行退款")
    public Result<Void> approve(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        Long adminId = BaseContext.getCurrentId();
        if (adminId == null) adminId = 0L;
        String remark = body != null ? body.get("handleRemark") : null;
        refundService.approve(id, adminId, 0, remark);
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "拒绝退款")
    public Result<Void> reject(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        Long adminId = BaseContext.getCurrentId();
        if (adminId == null) adminId = 0L;
        String remark = body != null ? body.get("handleRemark") : null;
        refundService.reject(id, adminId, 0, remark);
        return Result.success();
    }
}
