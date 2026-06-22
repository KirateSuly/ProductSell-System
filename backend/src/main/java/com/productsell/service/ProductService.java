package com.productsell.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.productsell.entity.Product;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService extends IService<Product> {

    /**
     * 上传商品图片
     */
    String uploadImage(MultipartFile file);

    /**
     * 删除商品（同时删除关联的图片文件）
     */
    boolean deleteProduct(Long id);
}
