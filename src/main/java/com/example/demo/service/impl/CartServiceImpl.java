package com.example.demo.service.impl;

import com.example.demo.mapper.CartItemMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.pojo.entity.CartItem;
import com.example.demo.pojo.entity.Product;
import com.example.demo.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public void addItem(Long userId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) quantity = 1;
        Product p = productMapper.findById(productId);
        if (p == null || p.getStatus() == null || p.getStatus() != 1) {
            throw new IllegalArgumentException("商品不存在或已下架");
        }
        CartItem item = new CartItem();
        item.setUserId(userId);
        item.setProductId(productId);
        item.setQuantity(quantity);
        cartItemMapper.insert(item);
    }

    @Override
    public void setQuantity(Long userId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) quantity = 1;
        CartItem exist = cartItemMapper.findByUserAndProduct(userId, productId);
        if (exist == null) {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setProductId(productId);
            item.setQuantity(quantity);
            cartItemMapper.insert(item);
        } else {
            cartItemMapper.updateQuantity(userId, productId, quantity);
        }
    }

    @Override
    public void removeItem(Long userId, Long productId) {
        cartItemMapper.deleteByUserAndProduct(userId, productId);
    }

    @Override
    public void clear(Long userId) {
        cartItemMapper.clearByUser(userId);
    }

    @Override
    public List<CartItem> list(Long userId) {
        return cartItemMapper.listByUser(userId);
    }

    @Override
    public List<Map<String, Object>> listWithProduct(Long userId) {
        List<CartItem> items = cartItemMapper.listByUser(userId);
        List<Map<String, Object>> res = new ArrayList<>();
        for (CartItem ci : items) {
            Product p = productMapper.findById(ci.getProductId());
            if (p != null) {
                Map<String, Object> m = new HashMap<>();
                m.put("cartItem", ci);
                m.put("product", p);
                res.add(m);
            }
        }
        return res;
    }
}
