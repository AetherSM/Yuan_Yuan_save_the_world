package com.example.demo.controller;

import com.example.demo.pojo.entity.ProductOrder;
import com.example.demo.pojo.result.Result;
import com.example.demo.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/orders")
@Tag(name = "管理员-订单管理", description = "管理员查看与操作全平台订单")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/products")
    @Operation(summary = "搜索商品订单", description = "管理员按条件搜索全平台商品订单")
    public Result<List<ProductOrder>> searchProductOrders(
            @Parameter(description = "订单号") @RequestParam(required = false) String orderNo,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "商家ID") @RequestParam(required = false) Long sellerId,
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status
    ) {
        return Result.success(orderService.adminSearchAll(orderNo, userId, sellerId, status));
    }

    @PatchMapping("/products/{orderNo}/status")
    @Operation(summary = "强制修改商品订单状态", description = "管理员强制修改订单状态（含强制取消）")
    public Result<Void> updateProductOrderStatus(@PathVariable String orderNo, @RequestBody Map<String, Object> payload) {
        Integer status = payload.get("status") == null ? null : Integer.valueOf(String.valueOf(payload.get("status")));
        String cancelReason = payload.get("cancelReason") == null ? null : String.valueOf(payload.get("cancelReason"));
        orderService.adminUpdateStatus(orderNo, status, cancelReason);
        return Result.success();
    }
}

