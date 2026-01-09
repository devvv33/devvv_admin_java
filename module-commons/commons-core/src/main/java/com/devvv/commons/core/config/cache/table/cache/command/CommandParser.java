package com.devvv.commons.core.config.cache.table.cache.command;

import org.springframework.data.redis.core.RedisOperations;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Create by WangSJ on 2023/08/08
 *
 */
public class CommandParser {

    private final Collection<Command> redisCommands;
    public CommandParser(Collection<Command> redisCommands){
        this.redisCommands = redisCommands;
    }


    /**
     * 提交命令，
     * 将命令封装，提交到Redis的批处理或管道中
     */
    public void submitCommand(RedisOperations<String, String> operations) {
        for (Command cmd : redisCommands) {
            switch (cmd.getOp()) {
                case SET:
                    operations.boundValueOps(cmd.getCacheKey()).set(cmd.getCacheValue());
                    break;
                case SETEX:
                    operations.boundValueOps(cmd.getCacheKey()).set(cmd.getCacheValue(), cmd.getSeconds(), TimeUnit.SECONDS);
                    break;
                case EXPIRE:
                    operations.expire(cmd.getCacheKey(), Long.parseLong(cmd.getCacheValue()), TimeUnit.SECONDS);
                    break;
                case DEL:
                    operations.delete(cmd.getCacheKey());
                    break;
                case SETS:
                    operations.opsForValue().multiSet(cmd.getKeyvalues());
                    break;
                default:
                    break;
            }
        }
    }
}
