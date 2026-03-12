package com.example.demo.service.impl;

import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.WalletTransactionMapper;
import com.example.demo.pojo.entity.UserEntity;
import com.example.demo.pojo.entity.WalletTransaction;
import com.example.demo.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletTransactionMapper walletTransactionMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recharge(Long userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }
        UserEntity user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        BigDecimal before = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        BigDecimal after = before.add(amount);
        userMapper.updateBalance(userId, after);

        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setTransactionType(1);
        tx.setAmount(amount);
        tx.setBalanceBefore(before);
        tx.setBalanceAfter(after);
        tx.setDescription("充值");
        walletTransactionMapper.insert(tx);
    }

    @Override
    public List<WalletTransaction> list(Long userId) {
        return walletTransactionMapper.listByUser(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundToUser(Long userId, BigDecimal amount, String relatedOrderNo, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("退款金额必须大于0");
        }
        UserEntity user = userMapper.findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        BigDecimal before = user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
        BigDecimal after = before.add(amount);
        userMapper.updateBalance(userId, after);

        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setTransactionType(5); // 5-退款
        tx.setAmount(amount);
        tx.setBalanceBefore(before);
        tx.setBalanceAfter(after);
        tx.setRelatedOrderNo(relatedOrderNo);
        tx.setDescription(description != null ? description : "订单退款");
        walletTransactionMapper.insert(tx);
    }
}

