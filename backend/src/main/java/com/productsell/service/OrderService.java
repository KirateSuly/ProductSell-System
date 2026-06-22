package com.productsell.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.productsell.dto.OrderCreateDto;
import com.productsell.entity.Order;

public interface OrderService extends IService<Order> {

    /**
     * 创建订单（含明细，事务性）
     */
    Order createOrder(OrderCreateDto dto);

    /**
     * 更新订单（含明细，事务性）
     */
    Order updateOrder(Long id, OrderCreateDto dto);

    /**
     * 删除订单（含明细）
     */
    boolean deleteOrder(Long id);

    /**
     * 更新订单状态
     */
    boolean updateStatus(Long id, String status);
}
