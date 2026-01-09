package com.devvv.commons.common.dto.common;

import lombok.Data;

/**
 * Create by WangSJ on 2024/08/06
 */
@Data
public class PageForm {
    // 当前页
    private Integer pageNum;
    // 每页多少行
    private Integer pageSize;
    /** 分页索引 */
    private Integer offset;
    /** 每页条数 */
    private Integer limit;
}
