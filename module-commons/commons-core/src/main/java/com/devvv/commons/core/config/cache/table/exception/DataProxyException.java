package com.devvv.commons.core.config.cache.table.exception;

/**
 * Create by WangSJ on 2023/08/07
 */
public class DataProxyException extends Throwable {

    private static final long serialVersionUID = 1L;

    public DataProxyException() {
        super();
    }

    public DataProxyException(String message) {
        super(message);
    }

    public DataProxyException(String message, Throwable e) {
        super(message, e);
    }

    public DataProxyException(Throwable e) {
        super(e);
    }


    public static DataProxyException newIt(Throwable e){
        return new DataProxyException(e);
    }

}
