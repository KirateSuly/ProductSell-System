package com.productsell.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.productsell.common.BusinessException;
import com.productsell.common.Result;
import com.productsell.common.ResultCode;
import com.productsell.dto.CustomerQueryDto;
import com.productsell.dto.PageResult;
import com.productsell.entity.Customer;
import com.productsell.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 客户管理接口
 * <p>
 * 提供客户的增删改查功能。
 * 客户编号在创建时由后端自动生成（格式：KH + yyyyMMdd + 3 位序号），无需前端传入。
 * 提供 /api/customers/all 接口供订单创建时选择客户下拉使用。
 */
@Tag(name = "客户管理", description = "客户增删改查")
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /**
     * 分页查询客户
     * <p>
     * 支持 keyword 模糊搜索（匹配客户名称、编号、联系人、电话）。
     */
    @Operation(summary = "分页查询客户")
    @GetMapping
    public Result<PageResult<Customer>> list(CustomerQueryDto query) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(Customer::getCustomerName, query.getKeyword())
                    .or()
                    .like(Customer::getCustomerCode, query.getKeyword())
                    .or()
                    .like(Customer::getContactPerson, query.getKeyword())
                    .or()
                    .like(Customer::getPhone, query.getKeyword()));
        }
        wrapper.orderByDesc(Customer::getCreateTime);
        Page<Customer> result = customerService.page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return Result.success(PageResult.from(result));
    }

    /**
     * 获取所有客户（不分页）
     * <p>
     * 专为订单创建页面的客户下拉选择器设计，返回全量客户列表。
     */
    @Operation(summary = "获取所有客户（用于下拉选择）")
    @GetMapping("/all")
    public Result<java.util.List<Customer>> getAll() {
        return Result.success(customerService.list());
    }

    @Operation(summary = "获取单个客户")
    @GetMapping("/{id}")
    public Result<Customer> getById(@PathVariable Long id) {
        Customer customer = customerService.getById(id);
        if (customer == null) {
            throw new BusinessException(ResultCode.CUSTOMER_NOT_FOUND);
        }
        return Result.success(customer);
    }

    /**
     * 创建客户
     * <p>
     * 客户编号由 {@link com.productsell.utils.CodeGenerator#generateCustomerCode()} 自动生成，
     * 采用 "KH + 年月日 + 序号" 格式，线程安全。
     */
    @Operation(summary = "创建客户（自动生成编号）")
    @PostMapping
    public Result<Customer> create(@RequestBody Customer customer) {
        customerService.createCustomer(customer); // 内部自动生成 client_code
        return Result.success("创建成功", customer);
    }

    @Operation(summary = "更新客户")
    @PutMapping("/{id}")
    public Result<Customer> update(@PathVariable Long id, @RequestBody Customer customer) {
        Customer existing = customerService.getById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.CUSTOMER_NOT_FOUND);
        }
        customer.setId(id);
        customer.setCustomerCode(existing.getCustomerCode()); // 保持编号不变
        customerService.updateById(customer);
        return Result.success("更新成功", customer);
    }

    @Operation(summary = "删除客户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        if (!customerService.removeById(id)) {
            throw new BusinessException(ResultCode.CUSTOMER_NOT_FOUND);
        }
        return Result.success("删除成功", null);
    }
}
