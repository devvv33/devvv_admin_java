package com.devvv.commons.token.handler;

import cn.dev33.satoken.exception.*;
import com.devvv.commons.common.response.ErrorCode;
import com.devvv.commons.common.response.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Create by WangSJ on 2024/06/25
 */
@RestControllerAdvice
public class GlobalTokenExceptionHandler {

    /**
     * 鉴权相关异常拦截
     */
    @ExceptionHandler
    public ResponseEntity<Result> handlerException(SaTokenException e) {
        Result result = null;
        // 未登录
        if (e instanceof NotLoginException) {
            result = Result.build(ErrorCode.UN_AUTH);
        }
        // 无角色，或无权限
        if (e instanceof NotRoleException || e instanceof NotPermissionException) {
            result = Result.build(ErrorCode.NO_PERMISSION);
        }
        // SameToken无效
        if (e instanceof SameTokenInvalidException) {
            result = Result.build(ErrorCode.NOT_INNER_CLIENT);
        }
        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }
}
