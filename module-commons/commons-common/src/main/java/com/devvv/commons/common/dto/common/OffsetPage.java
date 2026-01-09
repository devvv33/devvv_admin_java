package com.devvv.commons.common.dto.common;

import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.Page;
import lombok.Data;

import java.util.List;
import java.util.function.Function;

/**
 * 分页DTO
 */
@Data
public class OffsetPage<T>  {

    /** 分页索引 */
    private Integer offset;
    /** 每页条数 */
    private Integer limit;

    // 是否还有更多数据
    private boolean hasMore;
    // 列表数据
    private List<T> list;

    public static <T> OffsetPage<T> build(List<T> list) {
        return build(list, 0);
    }
    public static <T> OffsetPage<T> build(List<T> list, Integer offset) {
        if (list instanceof Page<T> page) {
            OffsetPage<T> dto = new OffsetPage<>();
            dto.setOffset((int) page.getStartRow());
            dto.setLimit((int) (page.getEndRow() - page.getStartRow()));
            dto.setList(list);
            dto.setHasMore(!list.isEmpty());
            return dto;
        }
        OffsetPage<T> dto = new OffsetPage<>();
        dto.setOffset(ObjectUtil.defaultIfNull(offset, 0));
        dto.setLimit(list == null ? 0 : list.size());
        dto.setList(list);
        dto.setHasMore(list != null && !list.isEmpty());
        return dto;
    }

    public static <T, R> OffsetPage<R> build(List<T> list, Function<T, R> func) {
        if (list instanceof Page<T> page) {
            OffsetPage<R> dto = new OffsetPage<>();
            dto.setOffset((int) page.getStartRow());
            dto.setLimit((int) (page.getEndRow() - page.getStartRow()));
            dto.setList(list.stream().map(func).toList());
            dto.setHasMore(!list.isEmpty());
            return dto;
        }
        OffsetPage<R> dto = new OffsetPage<>();
        dto.setOffset(0);
        dto.setLimit(list == null ? 0 : list.size());
        if (list != null) {
            dto.setList(list.stream().map(func).toList());
        }
        dto.setHasMore(list != null && !list.isEmpty());
        return dto;
    }
    public <R> OffsetPage<R> convertTo(Function<T, R> func) {
        OffsetPage<R> dto = new OffsetPage<>();
        dto.setOffset(this.offset);
        dto.setLimit(this.limit);
        dto.setHasMore(this.hasMore);
        if (list != null) {
            dto.setList(list.stream().map(func).toList());
        }
        return dto;
    }
}
