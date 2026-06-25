package com.productsell.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.productsell.common.BusinessException;
import com.productsell.common.Result;
import com.productsell.common.ResultCode;
import com.productsell.dto.PageResult;
import com.productsell.entity.User;
import com.productsell.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理接口
 * <p>
 * 提供系统用户的增删改查功能。所有接口仅限 ADMIN 角色访问。
 * <p>
 * 安全要点：
 * <ul>
 *   <li>密码使用 BCrypt 加密存储，返回数据时移除密码字段</li>
 *   <li>创建用户时检测用户名唯一性</li>
 *   <li>更新用户时，"留空密码"表示不修改原密码</li>
 * </ul>
 */
@Tag(name = "用户管理", description = "系统用户增删改查")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 整个控制器仅管理员可访问
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 分页查询用户列表
     * <p>
     * 支持 keyword 模糊搜索（匹配用户名、姓名、手机号），按创建时间倒序排列。
     *
     * @param page    当前页码（从1开始）
     * @param size    每页记录数
     * @param keyword 可选搜索关键词
     * @return 分页结果
     */
    @Operation(summary = "分页查询用户列表")
    @GetMapping
    public Result<PageResult<User>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 模糊搜索：匹配用户名、姓名或手机号
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword)
                    .or()
                    .like(User::getPhone, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = userService.page(new Page<>(page, size), wrapper);
        return Result.success(PageResult.from(result));
    }

    /**
     * 获取单个用户详情
     * <p>
     * 出于安全考虑，返回数据中移除密码字段。
     */
    @Operation(summary = "获取单个用户")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setPassword(null); // 安全：不返回密码
        return Result.success(user);
    }

    /**
     * 创建用户
     * <p>
     * 检测用户名唯一性，密码使用 BCrypt 加密后存储。
     */
    @Operation(summary = "创建用户")
    @PostMapping
    public Result<User> create(@RequestBody User user) {
        // 用户名唯一性检测
        Long count = userService.count(
                new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }
        // 前端已做 SHA-256 脱敏，此处直接 BCrypt 加密（与 AuthController 登录校验保持一致）
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.save(user);
        user.setPassword(null); // 安全：不返回密码
        return Result.success("创建成功", user);
    }

    /**
     * 更新用户信息
     * <p>
     * 密码处理：若提交的密码为空，则保留原密码不变；否则使用新密码加密后更新。
     */
    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody User user) {
        User existing = userService.getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 禁止停用系统管理员
        if ("admin".equals(existing.getUsername()) && user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ResultCode.ADMIN_CANNOT_DISABLE);
        }
        // 空密码表示不修改
        if (StringUtils.hasText(user.getPassword())) {
            // 前端已做 SHA-256 脱敏，此处直接 BCrypt 加密（与 AuthController 登录校验保持一致）
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // MyBatis-Plus 更新时忽略 null 字段
        }
        user.setId(id);
        userService.updateById(user);
        user.setPassword(null);
        return Result.success("更新成功", user);
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        /*禁止删除admin*/
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException(ResultCode.ADMIN_CANNOT_DELETE);
        }
        if (!userService.removeById(id)) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return Result.success("删除成功", null);
    }
}
