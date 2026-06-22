package com.productsell.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.productsell.entity.Customer;

public interface CustomerService extends IService<Customer> {

    /**
     * 创建客户（自动生成编号）
     */
    boolean createCustomer(Customer customer);
}
