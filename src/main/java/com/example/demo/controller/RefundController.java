package com.example.demo.controller;

import com.example.demo.context.BaseContext;
import com.example.demo.constant.Messages;
import com.example.demo.pojo.dto.RefundWithOrderDTO;
import com.example.demo.pojo.entity.RefundRequest;
import com.example.demo.pojo.entity.UserEntity;
import com.example.demo.pojo.result.Result;
import com.example.demo.service.RefundService;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 退款接口：用户申请与查询、商家/跑腿员处理
 */
@RestController
@RequestMapping("/refunds")
@Tag(name = "退款", description = "用户申请退款、商家/跑腿员处理退款")
public class RefundController {

    @Autowired
    private RefundService refundService;
    @Autowired
    private UserService userService;

    @PostMapping("/apply")
    @Operation(summary = "申请退款", description = "用户对已支付订单申请退款")
    public Result<RefundRequest> apply(@RequestBody Map<String, Object> body) {
        Long userId = requireLogin();
        Integer orderType = body.get("orderType") == null ? null : Integer.valueOf(String.valueOf(body.get("orderType")));
        String orderNo = (String) body.get("orderNo");
        BigDecimal refundAmount = body.get("refundAmount") == null ? null : new BigDecimal(String.valueOf(body.get("refundAmount")));
        String reason = (String) body.get("reason");
        if (orderType == null || orderNo == null || refundAmount == null) {
            return Result.error("订单类型、订单号、退款金额不能为空");
        }
        RefundRequest req = refundService.apply(userId, orderType, orderNo, refundAmount, reason);
        return Result.success(req);
    }

    @GetMapping("/my")
    @Operation(summary = "我的退款列表", description = "当前用户发起的退款申请")
    public Result<List<RefundWithOrderDTO>> myList(
            @Parameter(description = "状态: 0-待处理 2-已退款 3-已拒绝") @RequestParam(required = false) Integer status) {
        Long userId = requireLogin();
        return Result.success(refundService.listMy(userId, status));
    }

    /** 商家：待处理的退款列表（本店铺商品订单） */
    @GetMapping("/seller")
    @Operation(summary = "商家-退款列表", description = "商家查看本店铺订单的退款申请")
    public Result<List<RefundWithOrderDTO>> sellerList(
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Long sellerId = requireSeller();
        return Result.success(refundService.listBySeller(sellerId, status));
    }

    /** 跑腿员：待处理的退款列表（本人接单的跑腿订单） */
    @GetMapping("/runner")
    @Operation(summary = "跑腿员-退款列表", description = "跑腿员查看本人接单的退款申请")
    public Result<List<RefundWithOrderDTO>> runnerList(
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Long runnerId = requireRunner();
        return Result.success(refundService.listByRunner(runnerId, status));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "同意退款", description = "商家/跑腿员/管理员同意并执行退款")
    public Result<Void> approve(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        Long handlerId = requireLogin();
        Integer userType = getCurrentUserType(handlerId);
        String remark = body != null ? body.get("handleRemark") : null;
        refundService.approve(id, handlerId, userType, remark);
        return Result.success();
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "拒绝退款", description = "商家/跑腿员/管理员拒绝退款")
    public Result<Void> reject(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        Long handlerId = requireLogin();
        Integer userType = getCurrentUserType(handlerId);
        String remark = body != null ? body.get("handleRemark") : null;
        refundService.reject(id, handlerId, userType, remark);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "退款详情", description = "根据ID查询退款申请详情")
    public Result<RefundRequest> getById(@PathVariable Long id) {
        RefundRequest req = refundService.getById(id);
        if (req == null) {
            return Result.error("退款申请不存在");
        }
        return Result.success(req);
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

    private Long requireRunner() {
        Long userId = requireLogin();
        UserEntity user = userService.findById(userId);
        if (user == null || user.getUserType() == null || user.getUserType() != 2) {
            throw new IllegalArgumentException("仅跑腿员可操作");
        }
        return userId;
    }

    private Integer getCurrentUserType(Long userId) {
        UserEntity user = userService.findById(userId);
        return user != null ? user.getUserType() : null;
    }
}
