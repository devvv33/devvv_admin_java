package com.devvv.commons.common.dto.common;

import lombok.Data;

import java.util.List;
import java.util.function.Function;

/**
 * 分页DTO
 */
@Data
public class PageVO<T>  {
    // 当前页
    private Integer pageNum;
    // 每页多少行
    private Integer pageSize;
    // 总数
    private Long total;
    // 总页数
    private Integer totalPage;
    // 列表数据
    private List<T> list;


    public static <T> PageVO<T> build(List<T> list) {
        if (list instanceof com.github.pagehelper.Page<T> page) {
            PageVO<T> dto = new PageVO<>();
            dto.setPageNum(page.getPageNum());
            dto.setPageSize(page.getPageSize());
            dto.setTotal(page.getTotal());
            dto.setTotalPage(page.getPages());
            dto.setList(list);
            return dto;
        }
        PageVO<T> dto = new PageVO<>();
        dto.setPageNum(1);
        dto.setPageSize(list == null?0: list.size());
        dto.setTotal(list == null ? 0L : list.size());
        dto.setTotalPage(1);
        dto.setList(list);
        return dto;
    }

    public static <T, R> PageVO<R> build(List<T> list, Function<T, R> func) {
        PageVO pageVO = build(list);
        if (list != null) {
            pageVO.setList(list.stream().map(func).toList());
        }
        return pageVO;
    }
    public <R> PageVO<R> convertTo(Function<T, R> func) {
        PageVO<R> dto = new PageVO<>();
        dto.setPageNum(this.pageNum);
        dto.setPageSize(this.pageSize);
        dto.setTotal(this.total);
        dto.setTotalPage(this.totalPage);
        if (list != null) {
            dto.setList(list.stream().map(func).toList());
        }
        return dto;
    }
}
