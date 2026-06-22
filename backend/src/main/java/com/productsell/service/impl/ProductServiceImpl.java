package com.productsell.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.productsell.common.BusinessException;
import com.productsell.common.ResultCode;
import com.productsell.entity.Product;
import com.productsell.mapper.ProductMapper;
import com.productsell.service.ProductService;
import com.productsell.utils.FileUploadUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final FileUploadUtils fileUploadUtils;

    @Override
    public String uploadImage(MultipartFile file) {
        return fileUploadUtils.uploadProductImage(file);
    }

    @Override
    public boolean deleteProduct(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        fileUploadUtils.deleteFile(product.getImageUrl());
        return removeById(id);
    }
}
