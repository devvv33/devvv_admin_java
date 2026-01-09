package com.devvv.commons.core.config.cache.table.cache.buffer;

import com.alibaba.fastjson2.JSONObject;
import com.devvv.commons.core.config.cache.table.cache.command.Command;
import com.devvv.commons.core.config.cache.table.cache.command.CommandParser;
import com.devvv.commons.core.config.datasource.transaction.busi.BusiTransactionResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Create by WangSJ on 2024/07/15
 */
@Slf4j
public class BufferedTableCacheTransactionResource implements BusiTransactionResource {
    private final List<Command> buffer = new ArrayList<>();
    private final RedisTemplate<String, String> redisTemplate;

    public BufferedTableCacheTransactionResource(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 缓存命令
     */
    public void addCommand(Command command) {
        // 如果buffer中有删除命令，不再添加set命令
        if (buffer.stream().filter(cmd -> cmd.getOp() == Command.Operation.DEL).anyMatch(cmd -> cmd.getCacheKey().equals(command.getCacheKey()))) {
            return;
        }
        buffer.add(command);
        if(log.isDebugEnabled()){
            log.debug("[表缓存]-  缓冲命令 {}", command);
        }
    }


    /**
     * 根据一组缓存KEY，从缓冲区获取一组最后一条命令, 按照传入的顺序返回，
     * 不存在的cache返回返回null补位
     * @return
     */
    public List<Command> getLastFromBuffer(String... cacheKeys) {
        List<Command> lastCommands = new ArrayList<>();
        for (String key : cacheKeys) {
            lastCommands.add(getLastFromBuffer(key));
        }
        return lastCommands;
    }

    /**
     * 根据缓存KEY，从缓冲区获取最后一条命令
     * 如果有delete命令，优选返回delete命令
     */
    public Command getLastFromBuffer(String cacheKey) {
        if(buffer.isEmpty()) {
            return null;
        }
        // 优先返回删除命令
        Command deleteCmd = buffer.stream()
                .filter(cmd -> cmd.getOp() == Command.Operation.DEL)
                .filter(cmd -> cmd.getCacheKey().equals(cacheKey))
                .findAny().orElse(null);
        if (deleteCmd != null) {
            return deleteCmd;
        }

        // 倒序循环
        for (int i = buffer.size() - 1; i >= 0; i--) {
            Command command = buffer.get(i);
            if (command.getOp() == Command.Operation.EXPIRE) {
                continue;
            }

            // if sets operation
            if(command.getOp() == Command.Operation.SETS){
                Map<String, String> keyValueMap = command.getKeyvalues();
                if (keyValueMap != null && keyValueMap.containsKey(cacheKey)) {
                    return Command.newSetCommand(cacheKey, keyValueMap.get(cacheKey));
                }
            }

            if(cacheKey.equals(command.getCacheKey())){
                return command;
            }
        }
        return null;
    }

    @Override
    public int order() {
        return -1;
    }

    @Override
    public void begin() throws Throwable {

    }

    @Override
    public void commit() throws Throwable {
        if (buffer.isEmpty()) {
            return;
        }

        // 所有Redis命令，等待统一提交到Redis的连接中
        CommandParser drtc = new CommandParser(buffer);
        // lock cache object
        synchronized (buffer) {
            try {
                // 事务方式提交
                redisTemplate.execute(new SessionCallback() {
                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {
                        // 开启Reids事务
                        operations.multi();
                        // 提交所有命令
                        drtc.submitCommand(operations);
                        // 统一执行
                        return operations.exec();
                    }
                });
                if (log.isDebugEnabled()) {
                    log.debug("[表缓存]- 提交缓冲区-批处理 {}", buffer.size());
                }
            } catch (UnsupportedOperationException e) {
                // 管道方式提交
                redisTemplate.executePipelined(new SessionCallback() {
                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {
                        // 提交所有命令
                        drtc.submitCommand(operations);
                        return null;
                    }
                });
                if (log.isDebugEnabled()) {
                    log.debug("[表缓存]- 提交缓冲区-Pipeline {}", buffer.size());
                }
            } catch (Exception e) {
                log.error("[表缓存]- 缓冲区提交失败！\n{}", JSONObject.toJSONString(buffer), e);
            }
            buffer.clear();
        }
    }

    @Override
    public void rollback() throws Throwable {
        if(log.isDebugEnabled()){
            log.debug("[表缓存]- Rollback 清空缓冲区");
        }
        buffer.clear();
    }
}
