package com.example.demo.mapper;

import com.example.demo.pojo.entity.ErrandOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ErrandOrderMapper {
    void insert(ErrandOrder order);
    int updateStatus(@Param("orderNo") String orderNo, @Param("status") Integer status, @Param("runnerId") Long runnerId);
    
    ErrandOrder findByOrderNo(String orderNo);
    ErrandOrder findById(@Param("orderId") Long orderId);
    List<ErrandOrder> listByStatus(Integer status);

    /** 跑腿广场：待审核/待接单，以及无跑腿员的异常进行中单，供展示与重新接单 */
    List<ErrandOrder> listOpenForPlaza();
    List<ErrandOrder> listByUser(@Param("userId") Long userId, @Param("status") Integer status);
    List<ErrandOrder> listByRunner(Long runnerId);

    List<ErrandOrder> search(@Param("userId") Long userId, @Param("runnerId") Long runnerId, @Param("status") Integer status);

    void updateStatusById(@Param("orderId") Long orderId, @Param("status") Integer status);

    /** 用户从「我的订单」移除（软删，不影响跑腿员/商家） */
    int hideFromUser(@Param("orderNo") String orderNo, @Param("userId") Long userId);

    /** 批量移除 */
    int batchHideFromUser(@Param("orderNos") List<String> orderNos, @Param("userId") Long userId);
}
