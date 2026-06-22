package com.productsell.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.productsell.common.BusinessException;
import com.productsell.common.Result;
import com.productsell.common.ResultCode;
import com.productsell.dto.PageResult;
import com.productsell.dto.ProductQueryDto;
import com.productsell.entity.Product;
import com.productsell.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 商品管理接口
 * <p>
 * 提供商品的增删改查和图片上传功能。
 * <p>
 * 商品编号在创建时自动生成（格式：CP + yyyyMMdd + 序号）。
 * 图片通过独立上传接口上传，返回 URL 后由前端写入商品表单提交。
 * 删除商品时会同时删除服务器上的商品图片文件。
 */
@Tag(name = "商品管理", description = "商品增删改查及图片上传")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 分页查询商品
     * <p>
     * 支持按关键词（名称/编号/品牌）、分类、状态筛选，按创建时间倒序排列。
     */
    @Operation(summary = "分页查询商品")
    @GetMapping
    public Result<PageResult<Product>> list(ProductQueryDto query) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        // 关键词模糊搜索：商品名称、编号、品牌
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(Product::getProductName, query.getKeyword())
                    .or()
                    .like(Product::getProductCode, query.getKeyword())
                    .or()
                    .like(Product::getBrand, query.getKeyword()));
        }
        if (StringUtils.hasText(query.getCategory())) {
            wrapper.eq(Product::getCategory, query.getCategory());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Product::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Product::getCreateTime);
        Page<Product> result = productService.page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return Result.success(PageResult.from(result));
    }

    @Operation(summary = "获取单个商品")
    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return Result.success(product);
    }

    /**
     * 创建商品
     * <p>
     * 商品编号自动生成：CP + 年月日 + 3 位序列号。
     */
    @Operation(summary = "创建商品")
    @PostMapping
    public Result<Product> create(@RequestBody Product product) {
        // 自动生成商品编号
        String code = "CP" + new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date())
                + String.format("%03d", System.currentTimeMillis() % 1000);
        product.setProductCode(code);
        productService.save(product);
        return Result.success("创建成功", product);
    }

    /**
     * 更新商品
     * <p>
     * 商品编号不可修改，保留原编号。
     */
    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable Long id, @RequestBody Product product) {
        Product existing = productService.getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        product.setId(id);
        product.setProductCode(existing.getProductCode()); // 保持编号不变
        productService.updateById(product);
        return Result.success("更新成功", product);
    }

    /**
     * 删除商品
     * <p>
     * 同时删除服务器上关联的商品图片文件。
     */
    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success("删除成功", null);
    }

    /**
     * 上传商品图片
     * <p>
     * 支持 JPG/PNG/GIF/WebP 格式，最大 5MB。
     * 文件以 UUID 重命名存储在 uploads/products/ 目录下。
     *
     * @param file 图片文件（multipart/form-data）
     * @return 图片访问 URL（如 /uploads/products/abc123.jpg）
     */
    @Operation(summary = "上传商品图片")
    @PostMapping("/upload-image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String url = productService.uploadImage(file);
        return Result.success("上传成功", url);
    }
}
