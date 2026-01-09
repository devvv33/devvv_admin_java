package com.devvv.commons.common.response;

/**
 * Create by WangSJ on 2019/03/11
 * 定义错误代码格式
 */
public interface IResult {
    // 操作代码
    Integer getCode();
    // 提示信息
    String getMsg();

    // 获取数据
    default Object getData() {
        return null;
    }
}
