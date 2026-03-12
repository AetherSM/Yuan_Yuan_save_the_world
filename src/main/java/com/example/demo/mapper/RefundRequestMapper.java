package com.example.demo.mapper;

import com.example.demo.pojo.entity.RefundRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RefundRequestMapper {
    void insert(RefundRequest req);

    RefundRequest findById(@Param("id") Long id);

    List<RefundRequest> listByApplicant(@Param("applicantId") Long applicantId, @Param("status") Integer status);

    /** 商家：查询关联到本商家商品订单的退款申请（order_type=1 且 order_id 属于本商家订单） */
    List<RefundRequest> listBySellerId(@Param("sellerId") Long sellerId, @Param("status") Integer status);

    /** 跑腿员：查询关联到本跑腿员跑腿订单的退款申请（order_type=2 且 order_id 属于本跑腿员订单） */
    List<RefundRequest> listByRunnerId(@Param("runnerId") Long runnerId, @Param("status") Integer status);

    List<RefundRequest> listAll(@Param("orderType") Integer orderType, @Param("status") Integer status);

    void updateStatus(@Param("id") Long id, @Param("status") Integer status,
                      @Param("handlerId") Long handlerId, @Param("handleRemark") String handleRemark);
}
