package com.productsell.dto;

import lombok.Data;

/**
 * 客户查询参数
 */
@Data
public class CustomerQueryDto {

    private Integer page = 1;
    private Integer size = 10;
    private String keyword;
}
