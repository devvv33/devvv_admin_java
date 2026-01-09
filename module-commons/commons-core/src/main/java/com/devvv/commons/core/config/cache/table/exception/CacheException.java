package com.devvv.commons.core.config.cache.table.exception;

/**
 * Create by WangSJ on 2023/08/07
 */
public class CacheException extends Exception{

    public static CacheException throwe(String msg) throws CacheException {
        throw new CacheException(msg);
    }

    public static CacheException throwe(String msg, Throwable e) throws CacheException {
        throw new CacheException(msg, e);
    }

    public CacheException(){
        super();
    }

    public CacheException(String message){
        super(message);
    }

    public CacheException(String message, Throwable e){
        super(message, e);
    }

    public CacheException(Throwable e){
        super(e);
    }
}
