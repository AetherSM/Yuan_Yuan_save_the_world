package com.example.demo.service.impl;

import com.example.demo.constant.Messages;
import com.example.demo.expection.NotExistException;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.pojo.dto.ProductCreateDTO;
import com.example.demo.pojo.entity.Product;
import com.example.demo.service.ProductService;
import com.example.demo.expection.NoPermissionException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;



import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Product> search(Integer categoryId, String keyword, String minPrice, String maxPrice, Integer status, Long sellerId, String sortBy, String order) {
        return productMapper.search(categoryId, keyword, minPrice, maxPrice, status, sellerId, sortBy, order);
    }

    @Override
    public Product findById(Long productId) {
        return productMapper.findById(productId);
    }

    @Override
    public Product create(Long sellerId, ProductCreateDTO dto) {
        if (!StringUtils.hasText(dto.getProductName())) {
            throw new IllegalArgumentException("商品名称不能为空");
        }
        if (dto.getCategoryId() == null) {
            throw new IllegalArgumentException("分类不能为空");
        }
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("价格必须大于等于0");
        }
        Product p = new Product();
        p.setSellerId(sellerId);
        p.setCategoryId(dto.getCategoryId());
        p.setProductName(dto.getProductName());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setOriginalPrice(dto.getOriginalPrice());
        p.setStock(dto.getStock() == null ? 0 : dto.getStock());
        p.setSalesCount(0);
        p.setMainImage(dto.getMainImage());
        p.setImages(writeImages(dto.getImages()));
        p.setShippingAddress(dto.getShippingAddress());
        p.setStatus(dto.getStatus() == null ? 2 : dto.getStatus());
        p.setCreateTime(LocalDateTime.now());
        p.setUpdateTime(LocalDateTime.now());
        productMapper.insert(p);
        return productMapper.findById(p.getProductId());
    }

    @Override
    public Product update(Long productId, Long sellerId, ProductCreateDTO dto) {
        Product existing = productMapper.findById(productId);
        if (existing == null) {
            throw new IllegalArgumentException(Messages.PRODUCT_NOT_FOUND);
        }
        if (!existing.getSellerId().equals(sellerId)) {
            throw new IllegalArgumentException(Messages.NO_PERMISSION_PRODUCT);
        }
        existing.setCategoryId(dto.getCategoryId());
        existing.setProductName(dto.getProductName());
        existing.setDescription(dto.getDescription());
        existing.setPrice(dto.getPrice());
        existing.setOriginalPrice(dto.getOriginalPrice());
        existing.setStock(dto.getStock());
        existing.setMainImage(dto.getMainImage());
        existing.setImages(writeImages(dto.getImages()));
        existing.setShippingAddress(dto.getShippingAddress());
        existing.setStatus(dto.getStatus());
        existing.setUpdateTime(LocalDateTime.now());
        productMapper.update(existing);
        return productMapper.findById(productId);
    }

    @Override
    public void updateStatus(Long productId, Integer status, Long sellerId) {
        Product existing = productMapper.findById(productId);
        if (existing == null) {
            throw new NotExistException(Messages.PRODUCT_NOT_FOUND);
        }
        if (!existing.getSellerId().equals(sellerId)) {
            throw new NoPermissionException(Messages.NO_PERMISSION_PRODUCT);
        }
        productMapper.updateStatus(productId, status);
    }

    @Override
    public void adjustStock(Long productId, Integer stock, Integer delta, Long sellerId) {
        Product existing = productMapper.findById(productId);
        if (existing == null) {
            throw new NotExistException(Messages.PRODUCT_NOT_FOUND);
        }
        if (!existing.getSellerId().equals(sellerId)) {
            throw new NoPermissionException(Messages.NO_PERMISSION_PRODUCT);
        }
        if (stock == null && delta == null) {
            throw new IllegalArgumentException("请提供 stock 或 delta");
        }
        productMapper.updateStock(productId, stock, delta);
    }

    @Override
    public void updateStatusByAdmin(Long productId, Integer status) {
        Product existing = productMapper.findById(productId);
        if (existing == null) {
            throw new NotExistException(Messages.PRODUCT_NOT_FOUND);
        }
        productMapper.updateStatus(productId, status);
    }

    private String writeImages(List<String> images) {
        if (images == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("图片列表格式错误");
        }
    }
}
