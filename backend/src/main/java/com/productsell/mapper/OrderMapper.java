package com.productsell.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.productsell.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT MAX(order_no) FROM `order` WHERE order_no LIKE CONCAT(#{prefix}, '%')")
    String selectMaxCodeByPrefix(String prefix);
}
