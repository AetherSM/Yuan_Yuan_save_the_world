package com.example.demo.controller;

import com.example.demo.context.BaseContext;
import com.example.demo.pojo.dto.CouponDistributeDTO;
import com.example.demo.pojo.entity.Coupon;
import com.example.demo.pojo.entity.UserEntity;
import com.example.demo.pojo.entity.UserCoupon;
import com.example.demo.pojo.result.Result;
import com.example.demo.service.CouponService;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优惠券管理控制器
 */
@Tag(name = "优惠券管理", description = "优惠券的创建、领取和查询接口")
@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserService userService;

    /**
     * 创建优惠券
     * @param coupon 优惠券信息
     * @return 操作结果
     */
    @Operation(summary = "创建优惠券", description = "商家或管理员创建新的优惠券")
    @PostMapping("/add")
    public Result<String> addCoupon(@RequestBody Coupon coupon) {
        Long userId = requireLogin();
        UserEntity user = requireCurrentUser(userId);
        if (user.getUserType() != null && user.getUserType() == 0) {
            couponService.createCouponByAdmin(userId, coupon);
        } else if (user.getUserType() != null && user.getUserType() == 3) {
            couponService.saveCouponByMerchant(userId, coupon);
        } else {
            return Result.error("仅管理员或商家可创建优惠券");
        }
        return Result.success("优惠券创建成功");
    }

    @Operation(summary = "管理员创建平台券", description = "管理员创建平台统一优惠券")
    @PostMapping("/admin/create")
    public Result<String> adminCreateCoupon(@RequestBody Coupon coupon) {
        Long userId = requireLogin();
        couponService.createCouponByAdmin(userId, coupon);
        return Result.success("平台券创建成功");
    }

    @Operation(summary = "管理员创建/编辑平台券", description = "管理员创建或编辑平台优惠券")
    @PostMapping("/admin/save")
    public Result<String> adminSaveCoupon(@RequestBody Coupon coupon) {
        Long userId = requireLogin();
        couponService.saveCouponByAdmin(userId, coupon);
        return Result.success(coupon.getCouponId() == null ? "平台券创建成功" : "平台券更新成功");
    }

    @Operation(summary = "商家创建/编辑店铺券", description = "商家创建或编辑自己店铺优惠券")
    @PostMapping("/merchant/save")
    public Result<String> merchantSaveCoupon(@RequestBody Coupon coupon) {
        Long userId = requireLogin();
        couponService.saveCouponByMerchant(userId, coupon);
        return Result.success(coupon.getCouponId() == null ? "店铺券创建成功" : "店铺券更新成功");
    }

    /**
     * 查询可领取的优惠券列表
     * @return 优惠券列表
     */
    @Operation(summary = "查询可领取优惠券", description = "获取当前所有有效且可领取的优惠券")
    @GetMapping("/list")
    public Result<List<Coupon>> listAvailable() {
        return Result.success(couponService.listAvailable());
    }

    @Operation(summary = "商家查询自己的优惠券", description = "商家查询自己创建的优惠券")
    @GetMapping("/merchant/list")
    public Result<List<Coupon>> listMerchantCoupons() {
        Long userId = requireLogin();
        return Result.success(couponService.listMerchantCoupons(userId));
    }

    @Operation(summary = "管理员查询全部优惠券", description = "管理员查看平台全部优惠券")
    @GetMapping("/admin/list")
    public Result<List<Coupon>> listAdminCoupons() {
        Long userId = requireLogin();
        return Result.success(couponService.listAllForAdmin(userId));
    }

    /**
     * 用户领取优惠券
     * @param userId 用户ID
     * @param couponId 优惠券ID
     * @return 操作结果
     */
    @Operation(summary = "领取优惠券", description = "用户领取指定的优惠券")
    @PostMapping("/receive")
    public Result<String> receiveCoupon(
            @Parameter(description = "用户ID（兼容旧版，可不传）") @RequestParam(required = false) Long userId,
            @Parameter(description = "优惠券ID", required = true) @RequestParam Long couponId) {
        if (userId == null) {
            userId = requireLogin();
        }
        couponService.receiveCoupon(userId, couponId);
        return Result.success("领取成功");
    }

    @Operation(summary = "管理员定向发放优惠券", description = "管理员将指定优惠券发给指定用户列表")
    @PostMapping("/admin/distribute")
    public Result<String> adminDistribute(@RequestBody CouponDistributeDTO dto) {
        Long userId = requireLogin();
        couponService.distributeCoupon(userId, 0, dto);
        return Result.success("发放成功");
    }

    @Operation(summary = "商家定向发放优惠券", description = "商家将自己店铺优惠券发给指定用户列表")
    @PostMapping("/merchant/distribute")
    public Result<String> merchantDistribute(@RequestBody CouponDistributeDTO dto) {
        Long userId = requireLogin();
        couponService.distributeCoupon(userId, 3, dto);
        return Result.success("发放成功");
    }

    /**
     * 查询我的优惠券
     * @param userId 用户ID
     * @param status 状态（可选）
     * @return 用户优惠券列表
     */
    @Operation(summary = "我的优惠券", description = "查询用户已领取的优惠券，可按状态筛选")
    @GetMapping("/my")
    public Result<List<UserCoupon>> listMyCoupons(
            @Parameter(description = "用户ID（兼容旧版，可不传）") @RequestParam(required = false) Long userId,
            @Parameter(description = "状态: 1-未使用, 2-已使用, 3-已过期") @RequestParam(required = false) Integer status) {
        if (userId == null) {
            userId = requireLogin();
        }
        return Result.success(couponService.listMyCoupons(userId, status));
    }

    private Long requireLogin() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        return userId;
    }

    private UserEntity requireCurrentUser(Long userId) {
        UserEntity user = userService.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }
}
