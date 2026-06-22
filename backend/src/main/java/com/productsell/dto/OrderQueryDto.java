package com.productsell.dto;

import lombok.Data;

/**
 * 订单查询参数
 */
@Data
public class OrderQueryDto {

    private Integer page = 1;
    private Integer size = 10;
    private String orderNo;
    private Long customerId;
    private String orderStatus;
    private String startDate;
    private String endDate;
}
