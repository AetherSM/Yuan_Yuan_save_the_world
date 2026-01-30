package com.example.demo.service.impl;

import com.example.demo.mapper.ErrandOrderMapper;
import com.example.demo.pojo.entity.ErrandOrder;
import com.example.demo.service.ErrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class ErrandServiceImpl implements ErrandService {

    @Autowired
    private ErrandOrderMapper errandOrderMapper;
    
    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    @Override
    public void createOrder(ErrandOrder order) {
        order.setOrderNo("EO" + LocalDateTime.now().format(ORDER_NO_FMT) + (1000 + RANDOM.nextInt(9000)));
        order.setOrderStatus(1); // 待接单
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
}
