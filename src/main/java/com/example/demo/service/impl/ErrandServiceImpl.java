package com.example.demo.service.impl;

import com.example.demo.mapper.ErrandOrderMapper;
import com.example.demo.pojo.entity.ErrandOrder;
import com.example.demo.service.ErrandService;
import com.example.demo.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class ErrandServiceImpl implements ErrandService {

    @Autowired
    private ErrandOrderMapper errandOrderMapper;

    @Autowired
    private WalletService walletService;

    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    @Override
    public void createOrder(ErrandOrder order) {
        validateCreateOrder(order);
        order.setOrderNo("EO" + LocalDateTime.now().format(ORDER_NO_FMT) + (1000 + RANDOM.nextInt(9000)));
        order.setOrderStatus(0); // 待审核
        if (order.getTip() == null) {
            order.setTip(BigDecimal.ZERO);
        }
        order.setTotalAmount(order.getReward().add(order.getTip()));
        errandOrderMapper.insert(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void takeOrder(String orderNo, Long runnerId) {
        int updated = errandOrderMapper.updateStatus(orderNo, 2, runnerId);
        if (updated == 0) {
            throw new IllegalStateException("接单失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(String orderNo) {
        ErrandOrder order = errandOrderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (order.getOrderStatus() != 3) {
            throw new IllegalArgumentException("订单状态不允许完成");
        }
        
        // 计算总金额（优先使用 totalAmount 字段，否则按赏金 + 小费计算）
        java.math.BigDecimal totalAmount = order.getTotalAmount();
        if (totalAmount == null) {
            java.math.BigDecimal reward = order.getReward() != null ? order.getReward() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal tip = order.getTip() != null ? order.getTip() : java.math.BigDecimal.ZERO;
            totalAmount = reward.add(tip);
        }
        
        // 从用户余额中扣除金额并记录流水
        walletService.deductForPayment(order.getUserId(), totalAmount, orderNo, "跑腿订单支付");
        
        // 给跑腿员增加余额并记录一条“收入”流水
        walletService.addIncome(order.getRunnerId(), totalAmount, orderNo, "跑腿订单收入");
        
        // 更新订单状态为已完成
        int updated = errandOrderMapper.updateStatus(orderNo, 4, null);
        if (updated == 0) {
            throw new IllegalStateException("完成失败");
        }
    }

    @Override
    public List<ErrandOrder> listOpenOrders() {
        return errandOrderMapper.listByStatus(1);
    }

    @Override
    public List<ErrandOrder> listMyOrders(Long userId, Integer status) {
        return errandOrderMapper.listByUser(userId, status);
    }

    @Override
    public List<ErrandOrder> listRunnerOrders(Long runnerId) {
        return errandOrderMapper.listByRunner(runnerId);
    }

    @Override
    public List<ErrandOrder> searchErrands(Long userId, Long runnerId, Integer status) {
        return errandOrderMapper.search(userId, runnerId, status);
    }

    @Override
    public void updateErrandStatusByAdmin(Long orderId, Integer status) {
        errandOrderMapper.updateStatusById(orderId, status);
    }

    private void validateCreateOrder(ErrandOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("订单信息不能为空");
        }
        if (!StringUtils.hasText(order.getTitle())) {
            throw new IllegalArgumentException("标题不能为空");
        }
        if (!StringUtils.hasText(order.getPickupAddress())) {
            throw new IllegalArgumentException("取件地址不能为空");
        }
        if (!StringUtils.hasText(order.getDeliveryAddress())) {
            throw new IllegalArgumentException("送达地址不能为空");
        }
        if (!StringUtils.hasText(order.getContactName())) {
            throw new IllegalArgumentException("联系人不能为空");
        }
        if (!StringUtils.hasText(order.getContactPhone())) {
            throw new IllegalArgumentException("联系电话不能为空");
        }
        if (order.getReward() == null || order.getReward().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("赏金必须大于0");
        }
    }
}
