package com.devvv.commons.core.config.redis.redisson;

import com.devvv.commons.core.lock.MyGlobalRequestRedissonLock;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.config.Config;

/**
 * Create by WangSJ on 2023/07/17
 */
public class SysRedisson extends Redisson {

    public SysRedisson(Config config) {
        super(config);
    }

    /**
     * 获取自定义的 请求锁
     */
    public RLock getMyGlobalRequestLock(String name) {
        return new MyGlobalRequestRedissonLock(commandExecutor, name);
    }
}
