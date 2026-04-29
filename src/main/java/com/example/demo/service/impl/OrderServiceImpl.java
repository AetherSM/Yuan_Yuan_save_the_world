package com.example.demo.service.impl;

import com.example.demo.mapper.OrderItemMapper;
import com.example.demo.mapper.AddressMapper;
import com.example.demo.mapper.CouponMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.mapper.ProductOrderMapper;
import com.example.demo.mapper.ShoppingRecordMapper;
import com.example.demo.mapper.UserCouponMapper;
import com.example.demo.pojo.dto.OrderCreateDTO;
import com.example.demo.pojo.dto.OrderItemDTO;
import com.example.demo.pojo.entity.OrderItem;
import com.example.demo.pojo.entity.Address;
import com.example.demo.pojo.entity.Coupon;
import com.example.demo.pojo.entity.Product;
import com.example.demo.pojo.entity.ProductOrder;
import com.example.demo.pojo.entity.ShoppingRecord;
import com.example.demo.pojo.entity.UserCoupon;
import com.example.demo.service.OrderService;
import com.example.demo.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductOrder createOrder(Long userId, OrderCreateDTO dto) {
        OrderCalcResult calc = calcOrder(dto);
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
        CouponApplyResult couponApply = applyCouponIfNeeded(userId, dto.getCouponId(), calc.totalAmount);

        ProductOrder order = new ProductOrder();
        order.setOrderNo(genOrderNo());
        order.setUserId(userId);
        order.setSellerId(calc.sellerId);
        order.setTotalAmount(couponApply.payAmount);
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

        for (OrderItem oi : calc.items) {
            oi.setOrderId(order.getOrderId());
        }
        orderItemMapper.batchInsert(calc.items);

        if (couponApply.userCoupon != null) {
            int updated = userCouponMapper.markUsed(couponApply.userCoupon.getId(), order.getOrderId());
            if (updated == 0) {
                throw new IllegalArgumentException("优惠券已被使用，请重新选择");
            }
        }

        // 扣减库存
        for (OrderItemDTO itemDTO : dto.getItems()) {
            productMapper.updateStock(itemDTO.getProductId(), null, -itemDTO.getQuantity());
        }

        return productOrderMapper.findByOrderNo(order.getOrderNo());
    }

    @Override
    public Map<String, Object> previewOrder(Long userId, OrderCreateDTO dto) {
        OrderCalcResult calc = calcOrder(dto);
        CouponApplyResult couponApply = applyCouponIfNeeded(userId, dto.getCouponId(), calc.totalAmount);
        return buildAmountPreview(calc.totalAmount, couponApply.discountAmount, couponApply.payAmount, dto.getCouponId());
    }

    @Override
    public List<Map<String, Object>> listAvailableCouponsForOrder(Long userId, OrderCreateDTO dto) {
        OrderCalcResult calc = calcOrder(dto);
        List<UserCoupon> myCoupons = userCouponMapper.listByUser(userId, 0);
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserCoupon uc : myCoupons) {
            Coupon coupon = couponMapper.findById(uc.getCouponId());
            if (coupon == null || !isCouponCurrentlyValid(coupon)) {
                continue;
            }
            BigDecimal discount = calcDiscount(coupon, calc.totalAmount);
            if (discount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal payAmount = calc.totalAmount.subtract(discount).max(new BigDecimal("0.01"));
            Map<String, Object> item = new HashMap<>();
            item.put("userCouponId", uc.getId());
            item.put("couponId", coupon.getCouponId());
            item.put("name", coupon.getName());
            item.put("type", coupon.getType());
            item.put("value", coupon.getValue());
            item.put("minSpend", coupon.getMinSpend());
            item.put("endTime", coupon.getEndTime());
            item.put("discountAmount", discount);
            item.put("payAmount", payAmount);
            result.add(item);
        }
        result.sort(Comparator.comparing(
                (Map<String, Object> m) -> (BigDecimal) m.get("discountAmount")).reversed());
        return result;
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
        userCouponMapper.revertByOrderId(order.getOrderId());
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
        if (status == 5) {
            userCouponMapper.revertByOrderId(order.getOrderId());
        }
    }

    private OrderCalcResult calcOrder(OrderCreateDTO dto) {
        if (dto == null || CollectionUtils.isEmpty(dto.getItems())) {
            throw new IllegalArgumentException("订单商品不能为空");
        }
        Long sellerId = null;
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemDTO itemDTO : dto.getItems()) {
            Product product = productMapper.findById(itemDTO.getProductId());
            if (product == null || product.getStatus() == null || product.getStatus() != 1) {
                throw new IllegalArgumentException("商品不存在或未上架");
            }
            if (itemDTO.getQuantity() == null || itemDTO.getQuantity() <= 0) {
                throw new IllegalArgumentException("商品数量必须大于0");
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
        return new OrderCalcResult(sellerId, total, items);
    }

    private CouponApplyResult applyCouponIfNeeded(Long userId, Long couponId, BigDecimal totalAmount) {
        if (couponId == null) {
            return new CouponApplyResult(null, BigDecimal.ZERO, totalAmount);
        }
        UserCoupon userCoupon = userCouponMapper.findUnusedByUserAndCoupon(userId, couponId);
        if (userCoupon == null) {
            throw new IllegalArgumentException("优惠券不可用或不属于当前用户");
        }
        Coupon coupon = couponMapper.findById(userCoupon.getCouponId());
        if (coupon == null || !isCouponCurrentlyValid(coupon)) {
            throw new IllegalArgumentException("优惠券不可用");
        }
        BigDecimal discount = calcDiscount(coupon, totalAmount);
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("当前订单不满足优惠券使用条件");
        }
        BigDecimal payAmount = totalAmount.subtract(discount).max(new BigDecimal("0.01"));
        return new CouponApplyResult(userCoupon, discount, payAmount);
    }

    private boolean isCouponCurrentlyValid(Coupon coupon) {
        if (coupon.getStatus() == null || coupon.getStatus() != 1) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getStartTime() != null && coupon.getStartTime().isAfter(now)) {
            return false;
        }
        return coupon.getEndTime() == null || !coupon.getEndTime().isBefore(now);
    }

    private BigDecimal calcDiscount(Coupon coupon, BigDecimal totalAmount) {
        BigDecimal minSpend = coupon.getMinSpend() == null ? BigDecimal.ZERO : coupon.getMinSpend();
        if (totalAmount.compareTo(minSpend) < 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal discount = BigDecimal.ZERO;
        if (coupon.getType() != null && coupon.getType() == 1) {
            discount = coupon.getValue() == null ? BigDecimal.ZERO : coupon.getValue();
        } else if (coupon.getType() != null && coupon.getType() == 2) {
            BigDecimal discountRate = coupon.getValue() == null ? BigDecimal.TEN : coupon.getValue();
            BigDecimal ratio = discountRate.compareTo(BigDecimal.ONE) > 0
                    ? discountRate.divide(BigDecimal.TEN, 4, RoundingMode.HALF_UP)
                    : discountRate;
            if (ratio.compareTo(BigDecimal.ZERO) <= 0 || ratio.compareTo(BigDecimal.ONE) >= 0) {
                return BigDecimal.ZERO;
            }
            discount = totalAmount.multiply(BigDecimal.ONE.subtract(ratio));
        }
        if (discount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal maxAllowed = totalAmount.subtract(new BigDecimal("0.01"));
        if (maxAllowed.compareTo(BigDecimal.ZERO) < 0) {
            maxAllowed = BigDecimal.ZERO;
        }
        if (discount.compareTo(maxAllowed) > 0) {
            discount = maxAllowed;
        }
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    private Map<String, Object> buildAmountPreview(BigDecimal originalAmount, BigDecimal discountAmount, BigDecimal payAmount, Long userCouponId) {
        Map<String, Object> result = new HashMap<>();
        result.put("originalAmount", originalAmount);
        result.put("discountAmount", discountAmount);
        result.put("payAmount", payAmount);
        result.put("couponId", userCouponId);
        return result;
    }

    private String genOrderNo() {
        return "PO" + LocalDateTime.now().format(ORDER_NO_FMT) + (1000 + RANDOM.nextInt(9000));
    }

    private static class OrderCalcResult {
        private final Long sellerId;
        private final BigDecimal totalAmount;
        private final List<OrderItem> items;

        private OrderCalcResult(Long sellerId, BigDecimal totalAmount, List<OrderItem> items) {
            this.sellerId = sellerId;
            this.totalAmount = totalAmount;
            this.items = items;
        }
    }

    private static class CouponApplyResult {
        private final UserCoupon userCoupon;
        private final BigDecimal discountAmount;
        private final BigDecimal payAmount;

        private CouponApplyResult(UserCoupon userCoupon, BigDecimal discountAmount, BigDecimal payAmount) {
            this.userCoupon = userCoupon;
            this.discountAmount = discountAmount;
            this.payAmount = payAmount;
        }
    }
}
