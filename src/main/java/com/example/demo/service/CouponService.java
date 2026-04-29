package com.example.demo.service;

import com.example.demo.pojo.dto.CouponDistributeDTO;
import com.example.demo.pojo.entity.Coupon;
import com.example.demo.pojo.entity.UserCoupon;
import java.util.List;

/**
 * 优惠券业务接口
 */
public interface CouponService {
    
    /**
     * 创建优惠券
     * @param coupon 优惠券信息
     */
    void createCoupon(Coupon coupon);

    /**
     * 管理员创建平台券
     */
    void createCouponByAdmin(Long adminId, Coupon coupon);

    /**
     * 管理员创建/编辑平台券
     */
    void saveCouponByAdmin(Long adminId, Coupon coupon);

    /**
     * 商家创建/编辑店铺券
     */
    void saveCouponByMerchant(Long merchantId, Coupon coupon);
    
    /**
     * 查询可领取的优惠券
     * @return 优惠券列表
     */
    List<Coupon> listAvailable();

    /**
     * 商家查询自己的优惠券
     */
    List<Coupon> listMerchantCoupons(Long merchantId);

    /**
     * 管理员查询全部优惠券
     */
    List<Coupon> listAllForAdmin(Long adminId);
    
    /**
     * 领取优惠券
     * @param userId 用户ID
     * @param couponId 优惠券ID
     */
    void receiveCoupon(Long userId, Long couponId);

    /**
     * 定向发放优惠券
     * @param operatorId 操作人ID
     * @param operatorType 操作人类型（0管理员,3商家）
     */
    void distributeCoupon(Long operatorId, Integer operatorType, CouponDistributeDTO dto);
    
    /**
     * 查询我的优惠券
     * @param userId 用户ID
     * @param status 状态 (可选)
     * @return 用户优惠券列表
     */
    List<UserCoupon> listMyCoupons(Long userId, Integer status);
}
