package com.devvv.commons.common.exception;

import com.devvv.commons.common.response.ErrorCode;
import com.devvv.commons.common.response.IResult;
import lombok.Getter;

/**
 * Create by WangSJ on 2024/01/16
 */
@Getter
public class BusiException extends RuntimeException implements IResult {
    protected Integer code;
    protected String innerMsg;      // 内部错误信息
    protected String openMsg;       // 外部展示的错误信息
    protected Object data;

    public BusiException(IResult resultCode){
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
        this.innerMsg = resultCode.getMsg();
        this.openMsg = resultCode.getMsg();
    }

    public BusiException(String openMsg) {
        super(openMsg);
        this.code = ErrorCode.ERR.getCode();
        this.innerMsg = openMsg;
        this.openMsg = openMsg;
    }
    public BusiException(String innerMsg, String openMsg) {
        super(innerMsg);
        this.code = ErrorCode.ERR.getCode();
        this.innerMsg = innerMsg;
        this.openMsg = openMsg;
    }

    public BusiException(IResult resultCode, String openMsg){
        super(openMsg);
        this.code = resultCode.getCode();
        this.innerMsg = openMsg;
        this.openMsg = openMsg;
    }

    public BusiException(IResult resultCode, String innerMsg, String openMsg) {
        super(innerMsg);
        this.code = resultCode.getCode();
        this.innerMsg = innerMsg;
        this.openMsg = openMsg;
    }

    public BusiException(IResult resultCode, String innerMsg, String openMsg, Object data) {
        super(innerMsg);
        this.code = resultCode.getCode();
        this.innerMsg = innerMsg;
        this.openMsg = openMsg;
        this.data = data;
    }


    @Override
    public String getMsg() {
        return this.openMsg;
    }

    /**
     * 自定义异常将不再爬取堆栈，提高异常性能
     */
    // @Override
    // public synchronized Throwable fillInStackTrace() {
    //     return this;
    // }
}
