package com.productsell.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private String orderStatus;
    private LocalDateTime orderDate;
    private String deliveryAddress;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
