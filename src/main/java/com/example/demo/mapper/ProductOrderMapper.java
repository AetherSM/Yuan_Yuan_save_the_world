package com.example.demo.mapper;

import com.example.demo.pojo.entity.ProductOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductOrderMapper {
    void insert(ProductOrder order);

    void updateStatus(@Param("orderNo") String orderNo,
                      @Param("orderStatus") Integer orderStatus,
                      @Param("cancelReason") String cancelReason);

    void markPay(@Param("orderNo") String orderNo);

    void markShip(@Param("orderNo") String orderNo);

    void markConfirm(@Param("orderNo") String orderNo);

    ProductOrder findByOrderNo(@Param("orderNo") String orderNo);

    List<ProductOrder> searchAll(@Param("orderNo") String orderNo,
                                 @Param("userId") Long userId,
                                 @Param("sellerId") Long sellerId,
                                 @Param("status") Integer status);

    List<ProductOrder> listByUser(@Param("userId") Long userId,
                                  @Param("status") Integer status);

    List<ProductOrder> listBySeller(@Param("sellerId") Long sellerId,
                                    @Param("status") Integer status);

    /** 列表查询：带订单商品摘要（itemSummary） */
    List<ProductOrder> listByUserWithItemSummary(@Param("userId") Long userId,
                                                 @Param("status") Integer status);
}

