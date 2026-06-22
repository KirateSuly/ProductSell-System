package com.productsell.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private Long total;
    private Long page;
    private Long size;
    private List<T> records;

    public static <T> PageResult<T> from(IPage<T> page) {
        return new PageResult<>(
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords()
        );
    }
}
