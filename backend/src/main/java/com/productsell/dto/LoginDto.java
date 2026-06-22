package com.productsell.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO
 * <p>
 * 密码由前端 SHA-256 哈希后传输，服务端不接收明文密码。
 * rememberMe 为 true 时 JWT 有效期延长为 7 天。
 */
@Data
public class LoginDto {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private Boolean rememberMe;
}
