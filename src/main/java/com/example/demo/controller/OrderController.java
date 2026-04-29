package com.example.demo.controller;

import com.example.demo.context.BaseContext;
import com.example.demo.constant.Messages;
import com.example.demo.pojo.dto.OrderCreateDTO;
import com.example.demo.pojo.entity.OrderItem;
import com.example.demo.pojo.entity.ProductOrder;
import com.example.demo.pojo.entity.UserEntity;
import com.example.demo.pojo.result.Result;
import com.example.demo.service.OrderService;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@Tag(name = "商品订单", description = "商品订单创建与管理接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    /**
     * 创建订单
     * @param dto 订单创建信息
     * @return 创建的订单
     */
    @PostMapping
    @Operation(summary = "创建订单", description = "创建新的商品订单")
    public Result<ProductOrder> create(@RequestBody OrderCreateDTO dto) {
        Long userId = requireLogin();
        return Result.success(orderService.createOrder(userId, dto));
    }

    @PostMapping("/preview")
    @Operation(summary = "预览下单金额", description = "预览订单原价、优惠金额与应付金额")
    public Result<Map<String, Object>> preview(@RequestBody OrderCreateDTO dto) {
        Long userId = requireLogin();
        return Result.success(orderService.previewOrder(userId, dto));
    }

    @PostMapping("/coupons/available")
    @Operation(summary = "查询下单可用优惠券", description = "按当前待下单商品计算可用优惠券列表")
    public Result<List<Map<String, Object>>> listAvailableCoupons(@RequestBody OrderCreateDTO dto) {
        Long userId = requireLogin();
        return Result.success(orderService.listAvailableCouponsForOrder(userId, dto));
    }

    /**
     * 支付订单
     * @param orderNo 订单号
     * @return 无
     */
    @PostMapping("/{orderNo}/pay")
    @Operation(summary = "支付订单", description = "模拟支付订单")
    public Result<Void> pay(@PathVariable String orderNo) {
        Long userId = requireLogin();
        orderService.pay(orderNo, userId);
        return Result.success();
    }

    /**
     * 取消订单
     * @param orderNo 订单号
     * @param body 包含取消原因
     * @return 无
     */
    @PostMapping("/{orderNo}/cancel")
    @Operation(summary = "取消订单", description = "取消未完成的订单")
    public Result<Void> cancel(@PathVariable String orderNo, @RequestBody(required = false) Map<String, String> body) {
        Long userId = requireLogin();
        String reason = body == null ? null : body.get("cancelReason");
        orderService.cancel(orderNo, userId, reason);
        return Result.success();
    }

    /**
     * 发货（仅商家）
     * @param orderNo 订单号
     * @return 无
     */
    @PostMapping("/{orderNo}/ship")
    @Operation(summary = "订单发货", description = "商家对订单进行发货操作")
    public Result<Void> ship(@PathVariable String orderNo) {
        Long sellerId = requireSeller();
        orderService.ship(orderNo, sellerId);
        return Result.success();
    }

    /**
     * 确认收货
     * @param orderNo 订单号
     * @return 无
     */
    @PostMapping("/{orderNo}/confirm")
    @Operation(summary = "确认收货", description = "用户确认收到商品")
    public Result<Void> confirm(@PathVariable String orderNo) {
        Long userId = requireLogin();
        orderService.confirm(orderNo, userId);
        return Result.success();
    }

    /**
     * 获取我的订单列表
     * @param status 订单状态过滤（可选）
     * @return 订单列表
     */
    @GetMapping
    @Operation(summary = "我的订单", description = "获取当前用户的订单列表")
    public Result<List<ProductOrder>> listMy(@Parameter(description = "订单状态: 1-待支付, 2-待发货, 3-待收货, 4-已完成, 5-已取消") @RequestParam(value = "status", required = false) Integer status) {
        Long userId = requireLogin();
        return Result.success(orderService.listUserOrders(userId, status));
    }

    /**
     * 获取商家订单列表（仅商家）
     * @param status 订单状态过滤（可选）
     * @return 订单列表
     */
    @GetMapping("/seller")
    @Operation(summary = "商家订单", description = "获取商家的订单列表（仅商家）")
    public Result<List<ProductOrder>> listSeller(@Parameter(description = "订单状态") @RequestParam(value = "status", required = false) Integer status) {
        Long sellerId = requireSeller();
        return Result.success(orderService.listSellerOrders(sellerId, status));
    }

    /**
     * 获取订单详情
     * @param orderNo 订单号
     * @return 订单详情（包含订单信息和订单项）
     */
    @GetMapping("/{orderNo}")
    @Operation(summary = "订单详情", description = "获取订单的详细信息及商品项")
    public Result<Map<String, Object>> detail(@PathVariable String orderNo) {
        ProductOrder order = orderService.findByOrderNo(orderNo);
        if (order == null) {
            return Result.error(Messages.ORDER_NOT_FOUND);
        }
        List<OrderItem> items = orderService.listItems(order.getOrderId());
        Map<String, Object> res = new HashMap<>();
        res.put("order", order);
        res.put("items", items);
        return Result.success(res);
    }

    private Long requireLogin() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new IllegalArgumentException(Messages.UNAUTHORIZED);
        }
        return userId;
    }

    private Long requireSeller() {
        Long userId = requireLogin();
        UserEntity user = userService.findById(userId);
        if (user == null || user.getUserType() == null || user.getUserType() != 3) {
            throw new IllegalArgumentException(Messages.ONLY_SELLER);
        }
        return userId;
    }
}
