package com.example.demo.service.impl;

import com.example.demo.mapper.ErrandOrderMapper;
import com.example.demo.mapper.OrderItemMapper;
import com.example.demo.mapper.ProductOrderMapper;
import com.example.demo.mapper.RefundRequestMapper;
import com.example.demo.pojo.dto.RefundWithOrderDTO;
import com.example.demo.pojo.entity.ErrandOrder;
import com.example.demo.pojo.entity.OrderItem;
import com.example.demo.pojo.entity.ProductOrder;
import com.example.demo.pojo.entity.RefundRequest;
import com.example.demo.service.RefundService;
import com.example.demo.service.WalletService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RefundServiceImpl implements RefundService {

    /** 商品订单状态：6-退款中 7-已退款 */
    private static final int PRODUCT_STATUS_REFUNDING = 6;
    private static final int PRODUCT_STATUS_REFUNDED = 7;
    /** 跑腿订单状态：7-退款中 8-已退款 */
    private static final int ERRAND_STATUS_REFUNDING = 7;
    private static final int ERRAND_STATUS_REFUNDED = 8;

    private static final int REFUND_STATUS_PENDING = 0;
    private static final int REFUND_STATUS_REFUNDED = 2;
    private static final int REFUND_STATUS_REJECTED = 3;

    @Autowired
    private RefundRequestMapper refundRequestMapper;
    @Autowired
    private ProductOrderMapper productOrderMapper;
    @Autowired
    private ErrandOrderMapper errandOrderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private WalletService walletService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundRequest apply(Long userId, Integer orderType, String orderNo, BigDecimal refundAmount, String reason) {
        if (orderType == null || (orderType != 1 && orderType != 2)) {
            throw new IllegalArgumentException("订单类型无效");
        }
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }
        if (orderType == 1) {
            ProductOrder order = productOrderMapper.findByOrderNo(orderNo);
            if (order == null || !order.getUserId().equals(userId)) {
                throw new IllegalArgumentException("订单不存在或无权限");
            }
            // 待发货、待收货、已完成可申请退款
            if (order.getOrderStatus() == null || (order.getOrderStatus() != 2 && order.getOrderStatus() != 3 && order.getOrderStatus() != 4)) {
                throw new IllegalArgumentException("当前订单状态不允许申请退款");
            }
            if (order.getOrderStatus() == PRODUCT_STATUS_REFUNDING || order.getOrderStatus() == PRODUCT_STATUS_REFUNDED) {
                throw new IllegalArgumentException("该订单已申请退款或已退款");
            }
            if (refundAmount.compareTo(order.getTotalAmount()) > 0) {
                throw new IllegalArgumentException("退款金额不能超过订单金额");
            }
            RefundRequest req = new RefundRequest();
            req.setOrderType(1);
            req.setOrderId(order.getOrderId());
            req.setOrderNo(orderNo);
            req.setApplicantId(userId);
            req.setRefundAmount(refundAmount);
            req.setReason(reason);
            req.setStatus(0);
            refundRequestMapper.insert(req);
            productOrderMapper.updateStatus(orderNo, PRODUCT_STATUS_REFUNDING, "用户申请退款");
            return refundRequestMapper.findById(req.getId());
        } else {
            ErrandOrder order = errandOrderMapper.findByOrderNo(orderNo);
            if (order == null || !order.getUserId().equals(userId)) {
                throw new IllegalArgumentException("订单不存在或无权限");
            }
            if (order.getOrderStatus() == null || (order.getOrderStatus() != 2 && order.getOrderStatus() != 3 && order.getOrderStatus() != 4)) {
                throw new IllegalArgumentException("当前订单状态不允许申请退款");
            }
            if (order.getOrderStatus() == ERRAND_STATUS_REFUNDING || order.getOrderStatus() == ERRAND_STATUS_REFUNDED) {
                throw new IllegalArgumentException("该订单已申请退款或已退款");
            }
            BigDecimal maxRefund = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
            if (refundAmount.compareTo(maxRefund) > 0) {
                throw new IllegalArgumentException("退款金额不能超过订单金额");
            }
            RefundRequest req = new RefundRequest();
            req.setOrderType(2);
            req.setOrderId(order.getOrderId());
            req.setOrderNo(orderNo);
            req.setApplicantId(userId);
            req.setRefundAmount(refundAmount);
            req.setReason(reason);
            req.setStatus(0);
            refundRequestMapper.insert(req);
            errandOrderMapper.updateStatusById(order.getOrderId(), ERRAND_STATUS_REFUNDING);
            return refundRequestMapper.findById(req.getId());
        }
    }

    @Override
    public List<RefundWithOrderDTO> listMy(Long userId, Integer status) {
        return enrichList(refundRequestMapper.listByApplicant(userId, status));
    }

    @Override
    public List<RefundWithOrderDTO> listBySeller(Long sellerId, Integer status) {
        return enrichList(refundRequestMapper.listBySellerId(sellerId, status));
    }

    @Override
    public List<RefundWithOrderDTO> listByRunner(Long runnerId, Integer status) {
        return enrichList(refundRequestMapper.listByRunnerId(runnerId, status));
    }

    @Override
    public List<RefundWithOrderDTO> listAll(Integer orderType, Integer status) {
        return enrichList(refundRequestMapper.listAll(orderType, status));
    }

    /** 为退款列表填充订单摘要，便于识别「退的是什么」 */
    private List<RefundWithOrderDTO> enrichList(List<RefundRequest> list) {
        if (list == null || list.isEmpty()) return new ArrayList<>();
        return list.stream().map(this::enrich).collect(Collectors.toList());
    }

    private RefundWithOrderDTO enrich(RefundRequest r) {
        RefundWithOrderDTO dto = new RefundWithOrderDTO();
        BeanUtils.copyProperties(r, dto);
        if (r.getOrderType() == null) return dto;
        if (r.getOrderType() == 1) {
            List<OrderItem> items = orderItemMapper.listByOrderId(r.getOrderId());
            if (items != null && !items.isEmpty()) {
                String summary = items.stream()
                    .map(i -> (i.getProductName() != null ? i.getProductName() : "商品") + " x " + (i.getQuantity() != null ? i.getQuantity() : 0))
                    .collect(Collectors.joining("；"));
                dto.setOrderSummary(summary);
                dto.setOrderTitle(items.get(0).getProductName() != null ? items.get(0).getProductName() : "商品订单");
            } else {
                dto.setOrderSummary("暂无商品明细");
                dto.setOrderTitle("商品订单");
            }
        } else if (r.getOrderType() == 2) {
            ErrandOrder errand = errandOrderMapper.findById(r.getOrderId());
            if (errand != null) {
                dto.setOrderTitle(StringUtils.hasText(errand.getTitle()) ? errand.getTitle() : "跑腿订单");
                String desc = errand.getDescription();
                if (StringUtils.hasText(desc)) {
                    dto.setOrderSummary(desc.length() > 80 ? desc.substring(0, 80) + "…" : desc);
                } else {
                    dto.setOrderSummary(errand.getTitle());
                }
            } else {
                dto.setOrderTitle("跑腿订单");
                dto.setOrderSummary("—");
            }
        }
        return dto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long refundId, Long handlerId, Integer handlerUserType, String handleRemark) {
        RefundRequest req = refundRequestMapper.findById(refundId);
        if (req == null) {
            throw new IllegalArgumentException("退款申请不存在");
        }
        if (req.getStatus() != REFUND_STATUS_PENDING) {
            throw new IllegalArgumentException("该申请已处理");
        }
        checkCanHandle(req, handlerId, handlerUserType);
        walletService.refundToUser(req.getApplicantId(), req.getRefundAmount(), req.getOrderNo(), "订单退款");
        refundRequestMapper.updateStatus(refundId, REFUND_STATUS_REFUNDED, handlerId, handleRemark);
        if (req.getOrderType() == 1) {
            productOrderMapper.updateStatus(req.getOrderNo(), PRODUCT_STATUS_REFUNDED, "已退款");
        } else {
            errandOrderMapper.updateStatusById(req.getOrderId(), ERRAND_STATUS_REFUNDED);
        }
    }

    @Override
    public void reject(Long refundId, Long handlerId, Integer handlerUserType, String handleRemark) {
        RefundRequest req = refundRequestMapper.findById(refundId);
        if (req == null) {
            throw new IllegalArgumentException("退款申请不存在");
        }
        if (req.getStatus() != REFUND_STATUS_PENDING) {
            throw new IllegalArgumentException("该申请已处理");
        }
        checkCanHandle(req, handlerId, handlerUserType);
        refundRequestMapper.updateStatus(refundId, REFUND_STATUS_REJECTED, handlerId, handleRemark);
        if (req.getOrderType() == 1) {
            productOrderMapper.updateStatus(req.getOrderNo(), 4, "退款已拒绝");
        } else {
            errandOrderMapper.updateStatusById(req.getOrderId(), 4);
        }
    }

    /** 校验处理人是否有权限：管理员、或对应订单的商家/跑腿员 */
    private void checkCanHandle(RefundRequest req, Long handlerId, Integer handlerUserType) {
        if (handlerUserType != null && handlerUserType == 0) {
            return; // 管理员
        }
        if (req.getOrderType() == 1) {
            ProductOrder order = productOrderMapper.findByOrderNo(req.getOrderNo());
            if (order == null || !order.getSellerId().equals(handlerId)) {
                throw new IllegalArgumentException("仅该订单商家可处理");
            }
        } else {
            ErrandOrder order = errandOrderMapper.findByOrderNo(req.getOrderNo());
            if (order == null || !order.getRunnerId().equals(handlerId)) {
                throw new IllegalArgumentException("仅接单跑腿员可处理");
            }
        }
    }

    @Override
    public RefundRequest getById(Long id) {
        return refundRequestMapper.findById(id);
    }
}
