package com.example.demo.service.impl;

import com.example.demo.mapper.OrderItemMapper;
import com.example.demo.mapper.AddressMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.mapper.ProductOrderMapper;
import com.example.demo.mapper.ShoppingRecordMapper;
import com.example.demo.pojo.dto.OrderCreateDTO;
import com.example.demo.pojo.dto.OrderItemDTO;
import com.example.demo.pojo.entity.OrderItem;
import com.example.demo.pojo.entity.Address;
import com.example.demo.pojo.entity.Product;
import com.example.demo.pojo.entity.ProductOrder;
import com.example.demo.pojo.entity.ShoppingRecord;
import com.example.demo.service.OrderService;
import com.example.demo.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductOrderMapper productOrderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private ShoppingRecordMapper shoppingRecordMapper;

    @Autowired
    private WalletService walletService;

    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductOrder createOrder(Long userId, OrderCreateDTO dto) {
        if (CollectionUtils.isEmpty(dto.getItems())) {
            throw new IllegalArgumentException("订单商品不能为空");
        }
        Address address = null;
        if (dto.getAddressId() != null) {
            address = addressMapper.findById(dto.getAddressId());
            if (address == null) {
                throw new IllegalArgumentException("地址不存在");
            }
        } else {
            if (!StringUtils.hasText(dto.getDeliveryAddress())) {
                throw new IllegalArgumentException("收货地址不能为空");
            }
            if (!StringUtils.hasText(dto.getContactName()) || !StringUtils.hasText(dto.getContactPhone())) {
                throw new IllegalArgumentException("收货人信息不能为空");
            }
        }
        // 检查并计算
        Long sellerId = null;
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemDTO itemDTO : dto.getItems()) {
            Product product = productMapper.findById(itemDTO.getProductId());
            if (product == null || product.getStatus() == null || product.getStatus() != 1) {
                throw new IllegalArgumentException("商品不存在或未上架");
            }
            if (product.getStock() < itemDTO.getQuantity()) {
                throw new IllegalArgumentException("库存不足: " + product.getProductName());
            }
            if (sellerId == null) {
                sellerId = product.getSellerId();
            } else if (!sellerId.equals(product.getSellerId())) {
                throw new IllegalArgumentException("一个订单仅支持同一商家商品");
            }
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            total = total.add(subtotal);

            OrderItem oi = new OrderItem();
            oi.setProductId(product.getProductId());
            oi.setProductName(product.getProductName());
            oi.setProductImage(product.getMainImage());
            oi.setPrice(product.getPrice());
            oi.setQuantity(itemDTO.getQuantity());
            oi.setSubtotal(subtotal);
            items.add(oi);
        }

        ProductOrder order = new ProductOrder();
        order.setOrderNo(genOrderNo());
        order.setUserId(userId);
        order.setSellerId(sellerId);
        order.setTotalAmount(total);
        if (address != null) {
            order.setDeliveryAddress(address.getAddress());
            order.setContactName(address.getContactName());
            order.setContactPhone(address.getContactPhone());
        } else {
            order.setDeliveryAddress(dto.getDeliveryAddress());
            order.setContactName(dto.getContactName());
            order.setContactPhone(dto.getContactPhone());
        }
        order.setOrderStatus(1);
        order.setRemark(dto.getRemark());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        productOrderMapper.insert(order);

        for (OrderItem oi : items) {
            oi.setOrderId(order.getOrderId());
        }
        orderItemMapper.batchInsert(items);

        // 扣减库存
        for (OrderItemDTO itemDTO : dto.getItems()) {
            productMapper.updateStock(itemDTO.getProductId(), null, -itemDTO.getQuantity());
        }

        return productOrderMapper.findByOrderNo(order.getOrderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(String orderNo, Long userId) {
        ProductOrder order = productOrderMapper.findByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在或无权限");
        }
        if (order.getOrderStatus() != 1) {
            throw new IllegalArgumentException("订单状态不允许支付");
        }
        
        // 检查余额并扣款（余额不足会抛出异常，事务会自动回滚）
        walletService.deductForPayment(userId, order.getTotalAmount(), orderNo, "订单支付");
        
        // 标记订单为已支付
        productOrderMapper.markPay(orderNo);
        
        // 记录购物记录
        List<OrderItem> items = orderItemMapper.listByOrderId(order.getOrderId());
        List<ShoppingRecord> records = new ArrayList<>();
        for (OrderItem oi : items) {
            ShoppingRecord r = new ShoppingRecord();
            r.setUserId(userId);
            r.setOrderNo(orderNo);
            r.setProductId(oi.getProductId());
            r.setProductName(oi.getProductName());
            r.setProductImage(oi.getProductImage());
            r.setPrice(oi.getPrice());
            r.setQuantity(oi.getQuantity());
            r.setSubtotal(oi.getSubtotal());
            records.add(r);
        }
        shoppingRecordMapper.batchInsert(records);
    }

    @Override
    public void cancel(String orderNo, Long userId, String reason) {
        ProductOrder order = productOrderMapper.findByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在或无权限");
        }
        if (order.getOrderStatus() != 1) {
            throw new IllegalArgumentException("仅待支付订单可取消");
        }
        productOrderMapper.updateStatus(orderNo, 5, reason);
    }

    @Override
    public void ship(String orderNo, Long sellerId) {
        ProductOrder order = productOrderMapper.findByOrderNo(orderNo);
        if (order == null || !order.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException("订单不存在或无权限");
        }
        if (order.getOrderStatus() != 2) {
            throw new IllegalArgumentException("仅待发货订单可发货");
        }
        productOrderMapper.markShip(orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(String orderNo, Long userId) {
        ProductOrder order = productOrderMapper.findByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("订单不存在或无权限");
        }
        if (order.getOrderStatus() != 3) {
            throw new IllegalArgumentException("仅待收货订单可确认");
        }
        productOrderMapper.markConfirm(orderNo);
        
        // 用户确认收货后，将订单金额转入商家钱包
        walletService.addIncome(order.getSellerId(), order.getTotalAmount(), orderNo, "订单完成收入");
    }

    @Override
    public ProductOrder findByOrderNo(String orderNo) {
        return productOrderMapper.findByOrderNo(orderNo);
    }

    @Override
    public List<ProductOrder> listUserOrders(Long userId, Integer status) {
        return productOrderMapper.listByUserWithItemSummary(userId, status);
    }

    @Override
    public List<ProductOrder> listSellerOrders(Long sellerId, Integer status) {
        return productOrderMapper.listBySeller(sellerId, status);
    }

    @Override
    public List<OrderItem> listItems(Long orderId) {
        return orderItemMapper.listByOrderId(orderId);
    }

    @Override
    public List<ProductOrder> adminSearchAll(String orderNo, Long userId, Long sellerId, Integer status) {
        return productOrderMapper.searchAll(orderNo, userId, sellerId, status);
    }

    @Override
    public void adminUpdateStatus(String orderNo, Integer status, String cancelReason) {
        if (!StringUtils.hasText(orderNo)) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        if (status == null) {
            throw new IllegalArgumentException("订单状态不能为空");
        }
        ProductOrder order = productOrderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        // 若强制取消，允许写入取消原因
        String reason = (status == 5) ? cancelReason : null;
        productOrderMapper.updateStatus(orderNo, status, reason);
    }

    private String genOrderNo() {
        return "PO" + LocalDateTime.now().format(ORDER_NO_FMT) + (1000 + RANDOM.nextInt(9000));
    }
}
