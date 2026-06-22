package com.productsell.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.productsell.entity.Customer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {

    /**
     * 查询指定前缀的最大客户编号，用于自动生成编号
     */
    @Select("SELECT MAX(customer_code) FROM customer WHERE customer_code LIKE CONCAT(#{prefix}, '%')")
    String selectMaxCodeByPrefix(String prefix);
}
