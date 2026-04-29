package com.example.demo.mapper;

import com.example.demo.pojo.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserCouponMapper {
    void insert(UserCoupon userCoupon);
    void updateStatus(Long id, Integer status, Long orderId);
    List<UserCoupon> listByUser(Long userId, Integer status);
    int countByUserAndCoupon(Long userId, Long couponId);
    UserCoupon findUnusedByUserAndCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);
    int markUsed(@Param("id") Long id, @Param("orderId") Long orderId);
    int revertByOrderId(@Param("orderId") Long orderId);
}
