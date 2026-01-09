package com.devvv.commons.core.mapping;

import java.util.Date;

/**
 * Create by WangSJ on 2024/12/10
 */
public interface BaseConvert {

    default Long dateToLong(Date date) {
        return date != null ? date.getTime() : null;
    }
    default Date longToDate(Long value) {
        return value != null ? new Date(value) : null;
    }

}
