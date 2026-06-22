package com.productsell.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.productsell.entity.User;
import com.productsell.mapper.UserMapper;
import com.productsell.utils.PasswordHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 应用启动数据初始化
 * <p>
 * 确保系统中存在默认管理员账号。
 * 每次启动都会将 admin 密码重置为 BCrypt(SHA-256("admin123"))，
 * 保证前端 SHA-256 脱敏传输后能与数据库中的密码匹配。
 * <p>
 * 默认账号：admin / admin123
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User admin = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "admin"));
        if (admin == null) {
            admin = new User();
            admin.setUsername("admin");
            admin.setRealName("系统管理员");
            admin.setRole("ADMIN");
            admin.setStatus(1);
            admin.setCreateTime(LocalDateTime.now());
        }
        admin.setPassword(passwordEncoder.encode(PasswordHashUtil.sha256("admin123")));
        admin.setUpdateTime(LocalDateTime.now());
        if (admin.getId() == null) {
            userMapper.insert(admin);
        } else {
            userMapper.updateById(admin);
        }
        log.info("管理员账号已就绪: admin / admin123");
    }
}
