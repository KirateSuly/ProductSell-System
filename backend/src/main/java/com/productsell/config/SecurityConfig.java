package com.productsell.config;

import com.productsell.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 核心配置
 * <p>
 * 安全策略：
 * <ul>
 *   <li>关闭 CSRF：前后端分离 + 无状态 JWT，不需要 CSRF 防护</li>
 *   <li>无状态 Session：每个请求独立携带 JWT Token，服务端不维护会话</li>
 *   <li>白名单：/api/auth/login、Swagger 文档、上传文件访问路径</li>
 *   <li>其余请求需认证：由 {@link JwtAuthenticationFilter} 进行 Token 验证</li>
 *   <li>方法级权限：通过 @PreAuthorize 注解控制（如 ADMIN 角色才能访问用户管理）</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 开启 @PreAuthorize 注解支持
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置安全过滤链
     * <p>
     * 过滤器执行顺序（关键）：
     * CorsFilter → JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter → Controller
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF（前后端分离 + JWT 无状态）
                .csrf(AbstractHttpConfigurer::disable)
                // 无状态会话管理
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // URL 权限规则
                .authorizeHttpRequests(auth -> auth
                        // 登录接口公开访问
                        .requestMatchers("/api/auth/login").permitAll()
                        // Swagger / SpringDoc 文档公开访问
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs/**").permitAll()
                        // 上传文件静态访问公开
                        .requestMatchers("/uploads/**").permitAll()
                        // 其余所有请求需要认证
                        .anyRequest().authenticated()
                )
                // 在 UsernamePasswordAuthenticationFilter 之前插入 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器
     * <p>
     * 使用 BCrypt 加密算法，强度因子为 10（默认）。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     * <p>
     * 供登录流程中的用户名/密码验证使用。
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
