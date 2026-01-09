package com.devvv.commons.core.config.redis.redisson;

import org.redisson.Redisson;
import org.redisson.config.Config;

/**
 * Create by WangSJ on 2023/07/17
 */
public class BizRedisson extends Redisson {

    public BizRedisson(Config config) {
        super(config);
    }
}
