package com.example.demo.service;

import com.example.demo.pojo.dto.ProductCreateDTO;
import com.example.demo.pojo.entity.Product;

import java.util.List;

public interface ProductService {


    /**

     * @param categoryId 分类ID

    void updateStatus(Long productId, Integer status, Long sellerId);

     * @param status 状态
     * @param sortBy 排序字段

     * @param order 排序方式
     * @return 商品列表
     */
    List<Product> search(Integer categoryId, String keyword, String minPrice, String maxPrice, Integer status, Long sellerId, String sortBy, String order);

    /**
     * 根据ID查询商品
     * @param productId 商品ID
     * @return 商品详情
     */
    Product findById(Long productId);

    /**
     * 发布商品
     * @param sellerId 卖家ID
     * @param dto 商品信息
     * @return 创建后的商品
     */
    Product create(Long sellerId, ProductCreateDTO dto);

    /**
     * 更新商品
     * @param productId 商品ID
     * @param sellerId 卖家ID (验证权限)
     * @param dto 更新信息
     * @return 更新后的商品
     */
    Product update(Long productId, Long sellerId, ProductCreateDTO dto);

    /**
     * 更新商品状态 (上架/下架)
     * @param productId 商品ID
     * @param status 新状态
     * @param sellerId 卖家ID
     */
    void updateStatus(Long productId, Integer status, Long sellerId) ;

    /**
     * 调整库存
     * @param productId 商品ID
     * @param stock 绝对库存值 (可选)
     * @param delta 库存增量 (可选)
     * @param sellerId 卖家ID
     */
    void adjustStock(Long productId, Integer stock, Integer delta, Long sellerId);

    void updateStatusByAdmin(Long productId, Integer status);
}
