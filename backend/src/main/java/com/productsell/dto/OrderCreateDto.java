package com.productsell.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单创建/编辑请求 DTO
 */
@Data
public class OrderCreateDto {

    @NotNull(message = "客户ID不能为空")
    private Long customerId;

    @NotNull(message = "下单日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime orderDate;

    private String deliveryAddress;
    private String remark;

    @NotEmpty(message = "订单明细不能为空")
    private List<OrderItemDto> items;
}
