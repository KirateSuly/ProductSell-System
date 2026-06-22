package com.productsell.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.productsell.entity.User;
import com.productsell.mapper.UserMapper;
import com.productsell.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
