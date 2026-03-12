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
    List<ErrandOrder> listByUser(@Param("userId") Long userId, @Param("status") Integer status);
    List<ErrandOrder> listByRunner(Long runnerId);

    List<ErrandOrder> search(@Param("userId") Long userId, @Param("runnerId") Long runnerId, @Param("status") Integer status);

    void updateStatusById(@Param("orderId") Long orderId, @Param("status") Integer status);
}
