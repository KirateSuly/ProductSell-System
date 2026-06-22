package com.productsell.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.productsell.entity.Customer;
import com.productsell.mapper.CustomerMapper;
import com.productsell.service.CustomerService;
import com.productsell.utils.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    private final CodeGenerator codeGenerator;

    @Override
    public boolean createCustomer(Customer customer) {
        customer.setCustomerCode(codeGenerator.generateCustomerCode());
        return save(customer);
    }
}
