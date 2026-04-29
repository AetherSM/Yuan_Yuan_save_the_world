package com.example.demo.mapper;

import com.example.demo.pojo.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CouponMapper {
    void insert(Coupon coupon);
    void update(Coupon coupon);
    Coupon findById(Long couponId);
    List<Coupon> findAvailable();
    List<Coupon> findByIssuer(@Param("issuerType") Integer issuerType, @Param("issuerId") Long issuerId);
    List<Coupon> findAll();
    int incrementReceiveCount(Long couponId);
}
