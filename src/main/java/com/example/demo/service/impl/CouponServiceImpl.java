package com.example.demo.service.impl;

import com.example.demo.constant.Messages;
import com.example.demo.expection.BusinessException;
import com.example.demo.mapper.CouponMapper;
import com.example.demo.mapper.UserCouponMapper;
import com.example.demo.pojo.dto.CouponDistributeDTO;
import com.example.demo.pojo.entity.Coupon;
import com.example.demo.pojo.entity.UserEntity;
import com.example.demo.pojo.entity.UserCoupon;
import com.example.demo.service.CouponService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponMapper couponMapper;
    
    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private UserService userService;

    @Override
    public void createCoupon(Coupon coupon) {
        normalizeCoupon(coupon);
        if (coupon.getIssuerType() == null) {
            coupon.setIssuerType(0);
        }
        couponMapper.insert(coupon);
    }

    @Override
    public void createCouponByAdmin(Long adminId, Coupon coupon) {
        requireRole(adminId, 0, "仅管理员可创建平台券");
        normalizeCoupon(coupon);
        coupon.setIssuerType(0);
        coupon.setIssuerId(adminId);
        couponMapper.insert(coupon);
    }

    @Override
    public void saveCouponByAdmin(Long adminId, Coupon coupon) {
        requireRole(adminId, 0, "仅管理员可创建或编辑平台券");
        normalizeCoupon(coupon);
        coupon.setIssuerType(0);
        coupon.setIssuerId(adminId);
        if (coupon.getCouponId() == null) {
            couponMapper.insert(coupon);
            return;
        }
        Coupon existing = couponMapper.findById(coupon.getCouponId());
        if (existing == null) {
            throw new IllegalArgumentException("优惠券不存在");
        }
        if (existing.getIssuerType() == null || existing.getIssuerType() != 0) {
            throw new IllegalArgumentException("仅可编辑平台券");
        }
        couponMapper.update(coupon);
    }

    @Override
    public void saveCouponByMerchant(Long merchantId, Coupon coupon) {
        requireRole(merchantId, 3, "仅商家可创建或编辑店铺券");
        normalizeCoupon(coupon);
        coupon.setIssuerType(1);
        coupon.setIssuerId(merchantId);
        if (coupon.getCouponId() == null) {
            couponMapper.insert(coupon);
            return;
        }
        Coupon existing = couponMapper.findById(coupon.getCouponId());
        if (existing == null) {
            throw new IllegalArgumentException("优惠券不存在");
        }
        if (existing.getIssuerType() == null || existing.getIssuerId() == null
                || existing.getIssuerType() != 1 || !existing.getIssuerId().equals(merchantId)) {
            throw new IllegalArgumentException("无权编辑该优惠券");
        }
        couponMapper.update(coupon);
    }

    private void normalizeCoupon(Coupon coupon) {
        if (coupon == null) {
            throw new IllegalArgumentException("优惠券参数不能为空");
        }
        if (coupon.getStatus() == null) {
            coupon.setStatus(1);
        }
        if (coupon.getReceivedCount() == null) {
            coupon.setReceivedCount(0);
        }
        if (coupon.getMinSpend() == null) {
            coupon.setMinSpend(java.math.BigDecimal.ZERO);
        }
    }

    @Override
    public List<Coupon> listAvailable() {
        return couponMapper.findAvailable();
    }

    @Override
    public List<Coupon> listMerchantCoupons(Long merchantId) {
        requireRole(merchantId, 3, "仅商家可查询店铺券");
        return couponMapper.findByIssuer(1, merchantId);
    }

    @Override
    public List<Coupon> listAllForAdmin(Long adminId) {
        requireRole(adminId, 0, "仅管理员可查询全部优惠券");
        return couponMapper.findAll();
    }

    @Override
    @Transactional
    public void receiveCoupon(Long userId, Long couponId) {
        Coupon c = couponMapper.findById(couponId);
        if (c == null || c.getStatus() == null || c.getStatus() != 1) {
            throw new BusinessException(Messages.COUPON_INVALID);
        }
        if (c.getStartTime() != null && c.getStartTime().isAfter(java.time.LocalDateTime.now())) {
            throw new BusinessException(Messages.COUPON_NOT_STARTED);
        }
        if (c.getEndTime() != null && c.getEndTime().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException(Messages.COUPON_EXPIRED);
        }
        int already = userCouponMapper.countByUserAndCoupon(userId, couponId);
        if (already > 0) {
            throw new BusinessException(Messages.COUPON_ALREADY_RECEIVED);
        }
        int updated = couponMapper.incrementReceiveCount(couponId);
        if (updated == 0) {
            throw new BusinessException(Messages.COUPON_OUT_OF_STOCK);
        }
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        userCouponMapper.insert(uc);
    }

    @Override
    @Transactional
    public void distributeCoupon(Long operatorId, Integer operatorType, CouponDistributeDTO dto) {
        if (dto == null || dto.getCouponId() == null || dto.getUserIds() == null || dto.getUserIds().isEmpty()) {
            throw new IllegalArgumentException("请传入优惠券ID和目标用户");
        }
        if (operatorType == null || (operatorType != 0 && operatorType != 3)) {
            throw new IllegalArgumentException("仅管理员或商家可发放优惠券");
        }
        requireRole(operatorId, operatorType, "操作人身份不合法");
        Coupon coupon = couponMapper.findById(dto.getCouponId());
        if (coupon == null) {
            throw new IllegalArgumentException("优惠券不存在");
        }
        if (operatorType == 3) {
            if (coupon.getIssuerType() == null || coupon.getIssuerId() == null
                    || coupon.getIssuerType() != 1 || !coupon.getIssuerId().equals(operatorId)) {
                throw new IllegalArgumentException("商家仅可发放自己创建的优惠券");
            }
        }
        Set<Long> uniqueUsers = new LinkedHashSet<>(dto.getUserIds());
        for (Long userId : uniqueUsers) {
            if (userId == null) {
                continue;
            }
            int already = userCouponMapper.countByUserAndCoupon(userId, coupon.getCouponId());
            if (already > 0) {
                continue;
            }
            int updated = couponMapper.incrementReceiveCount(coupon.getCouponId());
            if (updated == 0) {
                throw new BusinessException(Messages.COUPON_OUT_OF_STOCK);
            }
            UserCoupon uc = new UserCoupon();
            uc.setUserId(userId);
            uc.setCouponId(coupon.getCouponId());
            userCouponMapper.insert(uc);
        }
    }

    @Override
    public List<UserCoupon> listMyCoupons(Long userId, Integer status) {
        return userCouponMapper.listByUser(userId, status);
    }

    private void requireRole(Long userId, Integer expectUserType, String message) {
        UserEntity user = userService.findById(userId);
        if (user == null || user.getUserType() == null || !user.getUserType().equals(expectUserType)) {
            throw new IllegalArgumentException(message);
        }
    }
}
