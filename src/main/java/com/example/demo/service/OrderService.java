package com.example.demo.service;

import com.example.demo.pojo.dto.OrderCreateDTO;
import com.example.demo.pojo.entity.OrderItem;
import com.example.demo.pojo.entity.ProductOrder;

import java.util.List;

/**
 * 商品订单业务接口
 */
public interface OrderService {
    
    /**
     * 创建订单
     * @param userId 买家ID
     * @param dto 订单创建信息
     * @return 创建后的订单
     */
    ProductOrder createOrder(Long userId, OrderCreateDTO dto);

    /**
     * 支付订单
     * @param orderNo 订单号
     * @param userId 用户ID (验证权限)
     */
    void pay(String orderNo, Long userId);

    /**
     * 取消订单
     * @param orderNo 订单号
     * @param userId 用户ID
     * @param reason 取消原因
     */
    void cancel(String orderNo, Long userId, String reason);

    /**
     * 发货
     * @param orderNo 订单号
     * @param sellerId 卖家ID
     */
    void ship(String orderNo, Long sellerId);

    /**
     * 确认收货
     * @param orderNo 订单号
     * @param userId 买家ID
     */
    void confirm(String orderNo, Long userId);

    /**
     * 根据订单号查询
     * @param orderNo 订单号
     * @return 订单详情
     */
    ProductOrder findByOrderNo(String orderNo);

    /**
     * 查询用户订单列表
     * @param userId 买家ID
     * @param status 订单状态 (可选)
     * @return 订单列表
     */
    List<ProductOrder> listUserOrders(Long userId, Integer status);

    /**
     * 查询卖家订单列表
     * @param sellerId 卖家ID
     * @param status 订单状态 (可选)
     * @return 订单列表
     */
    List<ProductOrder> listSellerOrders(Long sellerId, Integer status);

    /**
     * 查询订单项列表
     * @param orderId 订单ID
     * @return 订单项列表
     */
    List<OrderItem> listItems(Long orderId);

    /**
     * 管理员：搜索全平台订单
     */
    List<ProductOrder> adminSearchAll(String orderNo, Long userId, Long sellerId, Integer status);

    /**
     * 管理员：强制修改订单状态（可用于取消/推进状态）
     */
    void adminUpdateStatus(String orderNo, Integer status, String cancelReason);
}
