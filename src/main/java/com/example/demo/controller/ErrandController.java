package com.example.demo.controller;

import com.example.demo.context.BaseContext;
import com.example.demo.pojo.entity.ErrandOrder;
import com.example.demo.pojo.result.Result;
import com.example.demo.service.ErrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 跑腿订单控制器
 */
@Tag(name = "跑腿订单管理", description = "跑腿订单的发布、接单与查询")
@RestController
@RequestMapping("/api/errands")
public class ErrandController {

    @Autowired
    private ErrandService errandService;

    /**
     * 创建跑腿订单
     * @param order 订单信息
     * @return 操作结果
     */
    @Operation(summary = "发布跑腿订单", description = "用户发布新的跑腿订单")
    @PostMapping("/create")
    public Result<String> createOrder(@RequestBody ErrandOrder order) {
        errandService.createOrder(order);
        return Result.success("订单创建成功");
    }

    /**
     * 跑腿员接单
     * @param orderNo 订单号
     * @param runnerId 跑腿员ID
     * @return 操作结果
     */
    @Operation(summary = "接单", description = "跑腿员接取订单")
    @PostMapping("/take")
    public Result<String> takeOrder(
            @Parameter(description = "订单号", required = true) @RequestParam String orderNo,
            @Parameter(description = "跑腿员ID", required = true) @RequestParam Long runnerId) {
        errandService.takeOrder(orderNo, runnerId);
        return Result.success("接单成功");
    }

    /**
     * 开始配送
     * @param orderNo 订单号
     * @return 操作结果
     */
    @Operation(summary = "开始配送", description = "跑腿员开始配送任务")
    @PostMapping("/start-delivery")
    public Result<String> startDelivery(
            @Parameter(description = "订单号", required = true) @RequestParam String orderNo) {
        errandService.startDelivery(orderNo);
        return Result.success("已开始配送");
    }

    /**
     * 完成订单
     * @param orderNo 订单号
     * @return 操作结果
     */
    @Operation(summary = "完成订单", description = "跑腿员或用户确认订单完成")
    @PostMapping("/complete")
    public Result<String> completeOrder(
            @Parameter(description = "订单号", required = true) @RequestParam String orderNo) {
        errandService.completeOrder(orderNo);
        return Result.success("订单完成");
    }

    /**
     * 查询待接单订单
     * @return 待接单订单列表
     */
    @Operation(summary = "查询跑腿广场订单", description = "待审核(仅展示)、待接单、以及无跑腿员的异常配送中单；不含已分配跑腿员的任务")
    @GetMapping("/open")
    public Result<List<ErrandOrder>> listOpenOrders() {
        return Result.success(errandService.listOpenOrders());
    }

    /**
     * 查询我的订单
     * @param userId 用户ID
     * @return 订单列表
     */
    @Operation(summary = "我的订单", description = "查询用户发布或接取的跑腿订单")
    @GetMapping("/my")
    public Result<List<ErrandOrder>> listMyOrders(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "订单状态: 不传=全部; 1-待接单; 2-已接单; 3-配送中; 4-已完成; 5-已取消; -1-进行中(待审核/待接单/已接单/配送中/退款中)", required = false) @RequestParam(required = false) Integer status) {
        return Result.success(errandService.listMyOrders(userId, status));
    }

    /**
     * 查询跑腿员已接单
     * @param runnerId 跑腿员ID
     * @return 订单列表
     */
    @Operation(summary = "跑腿任务", description = "查询跑腿员接取的订单")
    @GetMapping("/runner")
    public Result<List<ErrandOrder>> listRunnerOrders(
            @Parameter(description = "跑腿员ID", required = true) @RequestParam Long runnerId) {
        return Result.success(errandService.listRunnerOrders(runnerId));
    }

    @Operation(summary = "从我的跑腿订单移除", description = "仅隐藏当前用户跑腿订单列表中的记录，不删除订单数据")
    @PostMapping("/{orderNo}/hide-from-my-list")
    public Result<Void> hideFromMyList(@PathVariable String orderNo) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("未登录");
        }
        errandService.hideOrderFromUser(orderNo, userId);
        return Result.success();
    }

    @Operation(summary = "批量从我的跑腿订单移除")
    @PostMapping("/batch-hide")
    public Result<Void> batchHide(@RequestBody List<String> orderNos) {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return Result.error("未登录");
        }
        errandService.batchHideOrdersFromUser(orderNos, userId);
        return Result.success();
    }
}
