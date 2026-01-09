package com.devvv.commons.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Create by WangSJ on 2019/03/11
 * 基础通用的错误代码
 */
@Getter
@AllArgsConstructor
public enum ErrorCode implements IResult {

    //成功  通用
    SUCCESS(200, "success"),
    ERR(500, "服务器繁忙"),

    /**
     * 登录失败状态码
     */
    UN_AUTH(401,"未登录"),
    LOGIN_FAIL(402,"用户名或密码错误"),
    LOGIN_DISABLED(403,"用户已被禁用，请联系管理员"),
    LOGIN_FAIL_UNKNOW(404,"用户登录失败，原因未知"),

    NO_PERMISSION(405,"无此权限"),
    NO_HEADER(406,"无此权限"),              // 无请求头
    NOT_ALLOW_CLIENT(407,"无此权限"),       // 不允许的客户端类型
    NOT_INNER_CLIENT(408, "无此权限"),      // 必须内网访问

    CAPTCHA_CHECK_ERR(410,"验证码校验失败"),
    LOGIN_LOCK(411,"错误次数超出限制，已被锁定"),
    CHECK_PASSWORD_ERR(412,"密码过于简单，请修改密码！"),
    FREQUENT(429, "请求过于频繁"),

    LOGIC_ERROR(440, "逻辑错误"),
    PARAM_ERR(441, "无效参数"),


    /**
     * 系统异常
     */
    X_INFO(10101, "缺少请求头x-info"),
    X_ARG(10102, "缺少请求头x-arg"),
    ENCRYPTION_ERR(10103,"加密失败"),
    DECRYPTION_ERR(10104,"解密失败"),
    CRYPT_KEY_ERR(10105,"秘钥有误"),
    OUTPUTSTREAM_IO_EXCEPTION(10106, "您的主机中的软件中止了一个已建立的连接。"),
    UPLOAD_MAX_SIZE_ERROR(10107, "上传文件大小超过限制"),
    CLIENT_TIME_ERR(10108, "客户端时间错误"),
    REPEATED_REQUEST(10109, "重复请求"),

    /**
     * 加锁异常
     */
    // 分布式锁加锁失败 将抛出此异常
    LOCK_ERROR(10200, "服务器繁忙，请稍后再试"),
    // 分布式锁解锁失败 将抛出此异常
    UNLOCK_ERROR(10201, "服务器繁忙，请稍后再试"),

    /**
     * MQ异常
     */
    // MQ消息发送失败
    MQ_ERROR(10311, "服务器繁忙，请稍后再试"),

    /**
     * sql异常
     */
    SQL_BASE_ERROR(10401, "数据异常，操作失败"),
    // sql语法异常
    SQL_GRAMMAR_ERROR(10402, "服务器繁忙"),

    /**
     * Http请求异常
     */
    HTTP_BASE_ERROR(10500, "http请求失败"),
    HTTP_CLIENT_ACCEPT_ERROR(10501, "客户端Accept错误"),

    /**
     * 登录相关
     */
    LOGIN_BAN(20101, "禁止登录"),
    ;


    private final Integer code;
    private final String msg;
}