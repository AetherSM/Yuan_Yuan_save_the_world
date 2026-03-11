package com.example.demo.controller;

import com.example.demo.pojo.entity.Product;
import com.example.demo.pojo.result.Result;
import com.example.demo.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/products")
@Tag(name = "管理员-商品管理", description = "管理员对商品进行管理的接口")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    @Operation(summary = "搜索商品", description = "根据条件搜索平台所有商品")
    public Result<List<Product>> searchProducts(
            @Parameter(description = "分类ID") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "商家ID") @RequestParam(required = false) Long sellerId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        List<Product> products = productService.search(categoryId, keyword, null, null, status, sellerId, null, null);
        return Result.success(products);
    }

    @GetMapping("/pending")
    @Operation(summary = "查看待审核商品", description = "查看所有待审核的商品")
    public Result<List<Product>> listPendingProducts() {
        List<Product> products = productService.search(null, null, null, null, 2, null, null, null);
        return Result.success(products);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "更新商品状态", description = "强制上架或下架指定商品")
    public Result<Void> updateProductStatus(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        productService.updateStatusByAdmin(id, payload.get("status"));
        return Result.success();
    }

    @PatchMapping("/{id}/approve")
    @Operation(summary = "审核通过商品", description = "审核通过指定商品，设置为上架状态")
    public Result<Void> approveProduct(@PathVariable Long id) {
        productService.updateStatusByAdmin(id, 1);
        return Result.success();
    }

    @PatchMapping("/{id}/reject")
    @Operation(summary = "审核拒绝商品", description = "审核拒绝指定商品，设置为审核拒绝状态")
    public Result<Void> rejectProduct(@PathVariable Long id) {
        productService.updateStatusByAdmin(id, 3);
        return Result.success();
    }
}
