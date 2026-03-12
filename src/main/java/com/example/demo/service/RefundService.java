package com.example.demo.service;

import com.example.demo.pojo.dto.RefundWithOrderDTO;
import com.example.demo.pojo.entity.RefundRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * 退款业务接口
 */
public interface RefundService {

    /**
     * 用户申请退款
     * @param userId 申请人ID
     * @param orderType 订单类型 1-商品 2-跑腿
     * @param orderNo 订单号
     * @param refundAmount 退款金额
     * @param reason 退款原因
     */
    RefundRequest apply(Long userId, Integer orderType, String orderNo, BigDecimal refundAmount, String reason);

    /**
     * 用户：我的退款列表（含订单摘要，便于识别退的是什么）
     */
    List<RefundWithOrderDTO> listMy(Long userId, Integer status);

    /**
     * 商家：待处理的退款列表（本店铺商品订单，含订单摘要）
     */
    List<RefundWithOrderDTO> listBySeller(Long sellerId, Integer status);

    /**
     * 跑腿员：待处理的退款列表（本人接单的跑腿订单，含订单摘要）
     */
    List<RefundWithOrderDTO> listByRunner(Long runnerId, Integer status);

    /**
     * 管理员：全部退款列表（含订单摘要）
     */
    List<RefundWithOrderDTO> listAll(Integer orderType, Integer status);

    /**
     * 同意退款并执行退款（商家/跑腿员/管理员）
     * @param handlerUserType 处理人类型 0-管理员 2-跑腿员 3-商家
     */
    void approve(Long refundId, Long handlerId, Integer handlerUserType, String handleRemark);

    /**
     * 拒绝退款
     */
    void reject(Long refundId, Long handlerId, Integer handlerUserType, String handleRemark);

    RefundRequest getById(Long id);
}
