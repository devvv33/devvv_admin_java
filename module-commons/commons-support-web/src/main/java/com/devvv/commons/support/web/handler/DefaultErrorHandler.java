package com.devvv.commons.support.web.handler;

import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.devvv.commons.common.response.ErrorCode;
import com.devvv.commons.common.response.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Create by WangSJ on 2024/06/21
 */
@RestControllerAdvice
public class DefaultErrorHandler {


    /**
     * 使用 @Valid 参数验证 的错误信息处理
     * 如:
     * {
     *   "code": 442,
     *   "msg": "resCode[null] 不能为空"
     * }
     */
    @ExceptionHandler
    public ResponseEntity<Result> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Result<Object> result = Result.build(ErrorCode.PARAM_ERR.getCode(), "参数错误");

        Opt.of(ex)
                .map(MethodArgumentNotValidException::getBindingResult)
                .map(Errors::getFieldError)
                .map(fieldError -> StrUtil.format("{} {}", fieldError.getField(), fieldError.getDefaultMessage()))
                .ifPresent(result::setMsg);

        return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

}
