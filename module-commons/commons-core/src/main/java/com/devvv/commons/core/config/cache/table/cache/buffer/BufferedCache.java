package com.devvv.commons.core.config.cache.table.cache.buffer;

import com.devvv.commons.core.config.cache.table.cache.ICache;
import com.devvv.commons.core.config.cache.table.cache.command.Command;
import com.devvv.commons.core.config.cache.table.exception.CacheException;
import com.devvv.commons.core.config.datasource.transaction.busi.BusiTransactionResourceManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Create by WangSJ on 2024/07/15
 */
public class BufferedCache implements ICache {
    private final ICache delegate;
    private final RedisTemplate<String, String> redisTemplate;

    public BufferedCache(ICache delegate, RedisTemplate<String, String> redisTemplate) {
        this.delegate = delegate;
        this.redisTemplate = redisTemplate;
    }

    private static final String BUFFERED_CACHE_KEY = "BufferedTableCacheTransactionResource";

    // 获取事务资源绑定
    private BufferedTableCacheTransactionResource getResource() {
        BufferedTableCacheTransactionResource resource = BusiTransactionResourceManager.getResource(BUFFERED_CACHE_KEY);
        if (resource == null) {
            resource = new BufferedTableCacheTransactionResource(redisTemplate);
            BusiTransactionResourceManager.bindResource(BUFFERED_CACHE_KEY, resource);
        }
        return resource;
    }


    @Override
    public boolean set(String cacheKey, String value) throws CacheException {
        if (!BusiTransactionResourceManager.inTransaction()) {
            return delegate.set(cacheKey, value);
        }
        getResource().addCommand(Command.newSetCommand(cacheKey, value));
        return true;
    }

    @Override
    public boolean setex(String cacheKey, int seconds, String value) throws CacheException {
        if (!BusiTransactionResourceManager.inTransaction()) {
            return delegate.setex(cacheKey, seconds, value);
        }
        getResource().addCommand(Command.newSetExCommand(cacheKey, value, seconds));
        return false;
    }

    @Override
    public boolean del(String cacheKey) throws CacheException {
        if (!BusiTransactionResourceManager.inTransaction()) {
            return delegate.del(cacheKey);
        }
        getResource().addCommand(Command.newDelCommand(cacheKey));
        return false;
    }

    @Override
    public String get(String cacheKey) throws CacheException {
        if (!BusiTransactionResourceManager.inTransaction()) {
            return delegate.get(cacheKey);
        }
        Command command = getResource().getLastFromBuffer( cacheKey);

        // 缓冲区不存在命令，则交给委托处理
        if(command == null){
            return delegate.get(cacheKey);
        }
        // 如果是删除命令，则返回null
        if(command.getOp() == Command.Operation.DEL){
            return null;
        }

        // 如果是SET或MOD命令则返回最新的值
        return command.getCacheValue();
    }

    @Override
    public List<String> gets(String... cacheKey) throws CacheException {
        if (!BusiTransactionResourceManager.inTransaction()) {
            return delegate.gets(cacheKey);
        }

        String[] target = new String[cacheKey.length];
        List<Command> commands = getResource().getLastFromBuffer(cacheKey);
        // 提取不在缓冲区的命令
        // delegateIndex 索引槽用于target返回数据占位符
        // 如果缓冲区不存在命令，加入委托集
        List<String> delegateCacheKey = new ArrayList<String>();
        List<Integer> delegateIndex = new ArrayList<Integer>();
        for (int i = 0; i < cacheKey.length; i++) {
            String key = cacheKey[i];
            Command cmd = commands.get(i);
            if(null == cmd) {
                delegateCacheKey.add(key);
                delegateIndex.add(i);
                target[i] = null;
            } else {
                // 如果是删除命令，则返回null
                target[i] = (cmd.getOp() == Command.Operation.DEL) ? null : cmd.getCacheValue();
            }
        }

        // 缓冲区不存在的命令，则交给委托处理
        if(!delegateCacheKey.isEmpty()){
            List<String> parts = delegate.gets(delegateCacheKey.toArray(new String[] {}));
            for (int i = 0; i < delegateCacheKey.size(); i++) {
                target[delegateIndex.get(i)] = parts.get(i);
            }
        }
        return Arrays.asList(target);
    }

    @Override
    public boolean sets(Map<String, String> keyValueMap) throws CacheException {
        if (!BusiTransactionResourceManager.inTransaction()) {
            return delegate.sets(keyValueMap);
        }
        getResource().addCommand(Command.newSetsCommand(keyValueMap));
        return true;
    }

    @Override
    public boolean expire(String key, Integer seconds) throws CacheException {
        if (!BusiTransactionResourceManager.inTransaction()) {
            return delegate.expire(key, seconds);
        }
        getResource().addCommand(Command.newExpireCommand(key, seconds.toString()));
        return true;
    }
}
