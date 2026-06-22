package com.productsell.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.productsell.common.Result;
import com.productsell.common.ResultCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 认证过滤器
 * <p>
 * 在每个请求到达 Controller 之前执行，从 Authorization 请求头中提取 Bearer Token，
 * 验证 Token 有效性后将用户信息设置到 Spring Security 的 SecurityContext 中。
 * <p>
 * 关键行为：
 * <ul>
 *   <li>请求无 Token → 放行（由 SecurityConfig 的权限规则处理）</li>
 *   <li>Token 有效 → 加载用户信息，设置 SecurityContext</li>
 *   <li>Token 无效/过期 → 返回 HTTP 401 JSON 响应，不继续执行后续过滤器</li>
 * </ul>
 * <p>
 * 该过滤器在 {@link com.productsell.config.SecurityConfig} 中被注册到
 * UsernamePasswordAuthenticationFilter 之前。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 过滤器的核心逻辑
     * <p>
     * 1. 从请求头提取 Bearer Token
     * 2. 验证 Token 有效性
     * 3. 加载 UserDetails 并设置认证上下文
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    // Token 有效 → 加载用户信息
                    String username = jwtTokenProvider.getUsernameFromToken(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 设置认证上下文
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.warn("JWT 认证失败: {}", e.getMessage());
                sendUnauthorizedResponse(response, "Token 无效或已过期");
                return; // 阻止请求继续
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从 Authorization 头提取 Bearer Token
     * <p>
     * 格式：Authorization: Bearer eyJhbGciOi...
     *
     * @return Token 字符串，不存在则返回 null
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 返回 401 Unauthorized JSON 响应
     * <p>
     * 响应格式与全局 Result 包装器保持一致，前端 Axios 拦截器根据 401 状态码跳转登录页。
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Result<Void> result = Result.error(ResultCode.UNAUTHORIZED.getCode(), message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
