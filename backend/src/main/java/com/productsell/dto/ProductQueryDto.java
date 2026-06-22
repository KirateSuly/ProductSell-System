package com.productsell.dto;

import lombok.Data;

/**
 * 商品查询参数
 */
@Data
public class ProductQueryDto {

    private Integer page = 1;
    private Integer size = 10;
    private String keyword;
    private String category;
    private Integer status;
}
