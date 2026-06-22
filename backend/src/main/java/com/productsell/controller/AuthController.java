package com.productsell.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.productsell.common.BusinessException;
import com.productsell.common.Result;
import com.productsell.common.ResultCode;
import com.productsell.dto.LoginDto;
import com.productsell.dto.LoginResult;
import com.productsell.entity.User;
import com.productsell.mapper.UserMapper;
import com.productsell.security.JwtTokenProvider;
import com.productsell.security.LoginAttemptService;
import com.productsell.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证管理接口
 * <p>
 * 提供用户登录和获取当前登录用户信息的功能。
 * 登录接口为公开访问（在 SecurityConfig 中放行），其余接口需要携带 JWT Token。
 * <p>
 * 登录流程：
 * 1. 客户端 POST /api/auth/login 提交用户名和密码
 * 2. 服务端校验 → 生成 JWT Token → 返回 token + userInfo
 * 3. 客户端后续请求在 Authorization 头携带 token
 */
@Tag(name = "认证管理", description = "登录、用户信息相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptService loginAttemptService;

    @Value("${jwt.remember-me-expiration}")
    private long rememberMeExpiration;

    /**
     * 用户登录
     * <p>
     * 安全措施：
     * <ol>
     *   <li>密码脱敏传输：前端将密码 SHA-256 哈希后传给服务端，防止明文网络传输泄露</li>
     *   <li>暴力破解防护：同一 IP 15 分钟内最多 5 次失败尝试，超过后锁定</li>
     *   <li>密码存储：数据库中存储 BCrypt(SHA-256(password))，双重层加固</li>
     *   <li>记住我：勾选后 JWT 有效期延长为 7 天</li>
     * </ol>
     *
     * @param loginDto 包含 username、SHA-256(password)、rememberMe 的登录请求
     * @param request  Servlet 请求（用于获取客户端 IP）
     * @return JWT token + 用户基本信息
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResult> login(@Valid @RequestBody LoginDto loginDto,
                                      HttpServletRequest request) {
        String ip = getClientIp(request);

        // 安全检查：是否已被锁定
        if (loginAttemptService.isLocked(ip)) {
            throw new BusinessException(ResultCode.LOGIN_LOCKED);
        }

        // 1. 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, loginDto.getUsername()));

        if (user == null) {
            loginAttemptService.recordFailure(ip);
            int remaining = loginAttemptService.getRemainingAttempts(ip);
            if (remaining <= 0) {
                throw new BusinessException(ResultCode.LOGIN_LOCKED);
            }
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(),
                    "用户名或密码错误，还剩 " + remaining + " 次尝试机会");
        }

        // 2. 检查账号状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ACCOUNT_DISABLED);
        }

        // 3. 密码比对：前端传来的是 SHA-256(明文)，数据库存的是 BCrypt(SHA-256(明文))
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            loginAttemptService.recordFailure(ip);
            int remaining = loginAttemptService.getRemainingAttempts(ip);
            if (remaining <= 0) {
                throw new BusinessException(ResultCode.LOGIN_LOCKED);
            }
            throw new BusinessException(ResultCode.PASSWORD_ERROR.getCode(),
                    "用户名或密码错误，还剩 " + remaining + " 次尝试机会");
        }

        // 登录成功，清除失败记录
        loginAttemptService.clearAttempts(ip);

        // 4. 生成 JWT Token：记住我则使用 7 天过期
        boolean rememberMe = Boolean.TRUE.equals(loginDto.getRememberMe());
        long tokenExpiration = rememberMe ? rememberMeExpiration : jwtTokenProvider.getDefaultExpiration();
        String token = jwtTokenProvider.generateToken(user.getUsername(), user.getRole(), tokenExpiration);

        // 5. 构建响应（不含密码）
        LoginResult.UserInfo userInfo = LoginResult.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();

        LoginResult result = LoginResult.builder()
                .token(token)
                .userInfo(userInfo)
                .build();

        return Result.success("登录成功", result);
    }

    /**
     * 获取客户端真实 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取当前登录用户信息
     * <p>
     * 从 SecurityContext 中提取当前用户名，查询数据库返回用户信息。
     * 适用于前端刷新页面后恢复用户状态。
     *
     * @return 当前用户的基本信息（不含密码）
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userinfo")
    public Result<LoginResult.UserInfo> getUserInfo() {
        // 从 JWT 过滤器设置的 SecurityContext 中获取当前用户名
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        LoginResult.UserInfo userInfo = LoginResult.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .role(user.getRole())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();

        return Result.success(userInfo);
    }
}
