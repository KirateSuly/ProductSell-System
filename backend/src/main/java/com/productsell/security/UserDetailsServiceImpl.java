package com.productsell.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.productsell.entity.User;
import com.productsell.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Spring Security UserDetailsService 实现
 * <p>
 * 从 sys_user 表加载用户信息，供 Spring Security 的认证管理器使用。
 * 角色映射规则：数据库中的 "ADMIN" → Spring Security 权限 "ROLE_ADMIN"。
 * <p>
 * 账号状态检查通过 UserDetails 的 enabled 字段实现，
 * 禁用的账号（status=0）在认证时会自动被 Spring Security 拒绝。
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    /**
     * 根据用户名加载用户详情
     *
     * @param username 登录用户名
     * @return Spring Security UserDetails 对象
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 从数据库查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 构建 Spring Security UserDetails 对象
        // enabled: status=1 启用，status=0 禁用
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() != null && user.getStatus() == 1, // enabled
                true,  // accountNonExpired
                true,  // credentialsNonExpired
                true,  // accountNonLocked
                // 角色映射为 ROLE_ 前缀的 GrantedAuthority
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
