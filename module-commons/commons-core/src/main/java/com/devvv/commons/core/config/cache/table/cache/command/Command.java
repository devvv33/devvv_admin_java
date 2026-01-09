package com.devvv.commons.core.config.cache.table.cache.command;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * Create by WangSJ on 2023/08/07
 * <p>
 * 缓存命令的包装，同步或事物时非常有用
 */
@Data
@NoArgsConstructor
public class Command implements Serializable {
    @Serial
    private static final long serialVersionUID = 8538167070390761722L;
    private Operation op;

    /**
     * 缓存key
     */
    private String cacheKey;
    private String cacheValue;
    private Integer seconds;

    /**
     * 当操作是SETS时,keyvalues的缓存
     */
    private Map<String, String> keyvalues;

    /**
     * SET Command
     */
    public static Command newSetCommand(String cacheKey, String value) {
        return new Command(Operation.SET, cacheKey, value);
    }

    /**
     * SET And Expire Command
     */
    public static Command newSetExCommand(String cacheKey, String value, Integer seconds) {
        return new Command(Operation.SETEX, cacheKey, value, seconds);
    }

    /**
     * EXPIRE Command
     */
    public static Command newExpireCommand(String cacheKey, String value) {
        return new Command(Operation.EXPIRE, cacheKey, value);
    }

    /**
     * DEL Command
     */
    public static Command newDelCommand(String cacheKey) {
        return new Command(Operation.DEL, cacheKey);
    }

    /**
     * SETS Command
     */
    public static Command newSetsCommand(Map<String, String> keyValueMap) {
        return new Command(Operation.SETS, keyValueMap);
    }

    /**
     * 构造方法
     */
    private Command(Operation op, String cacheKey, String cacheValue) {
        this.op = op;
        this.cacheKey = cacheKey;
        this.cacheValue = cacheValue;
    }
    private Command(Operation op, String cacheKey, String cacheValue, Integer seconds) {
        this(op, cacheKey, cacheValue);
        this.seconds = seconds;
    }
    private Command(Operation op, String cacheKey) {
        this.op = op;
        this.cacheKey = cacheKey;
    }
    private Command(Operation op, Map<String, String> map) {
        this.op = op;
        this.keyvalues = map;
    }

    /**
     * 接口操作命令
     *
     * @author Killer
     */
    public enum Operation {

        /***
         * 单个缓存的操作类
         */
        SET,
        DEL,

        /**
         * 设置一个带有有效期的命令操作
         */
        SETEX,

        EXPIRE,

        /**
         * 一次设置多个key、values
         */
        SETS
    }
}