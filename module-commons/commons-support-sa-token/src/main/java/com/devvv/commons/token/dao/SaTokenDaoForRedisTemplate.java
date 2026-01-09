package com.devvv.commons.token.dao;

/**
 * Create by WangSJ on 2025/12/24
 */

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.auto.SaTokenDaoByObjectFollowString;
import cn.dev33.satoken.util.SaFoxUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Sa-Token 持久层实现 [ Redis 存储 ] (可用环境: SpringBoot2、SpringBoot3)
 *
 * Create by WangSJ on 2025/12/24
 *
 * 拷贝自:
 *         <dependency>
 *             <groupId>cn.dev33</groupId>
 *             <artifactId>sa-token-redis-template</artifactId>
 *             <version>1.44.0</version>
 *         </dependency>
 * 修改内容：
 *  这里我们把sa-token-redis 单独拷贝出来并进行修改
 *  主要原因是我们项目中，有多个RedisConnectionFactory，此处我们在初始化时要指定使用哪个Redis
 *  主要添加的代码：
 *      @Qualifier("sessionJedisConnectionFactory")
 */
@Component
public class SaTokenDaoForRedisTemplate implements SaTokenDaoByObjectFollowString, SaTokenDao {

    public StringRedisTemplate stringRedisTemplate;

    /**
     * 标记：当前 redis 连接信息是否已初始化成功
     */
    public boolean isInit;

    @Autowired
    public void init(@Qualifier("sessionJedisConnectionFactory") RedisConnectionFactory connectionFactory) {
        // 如果已经初始化成功了，就立刻退出，不重复初始化
        if (this.isInit) {
            return;
        }

        // 构建StringRedisTemplate
        StringRedisTemplate stringTemplate = new StringRedisTemplate();
        stringTemplate.setConnectionFactory(connectionFactory);
        stringTemplate.afterPropertiesSet();
        this.stringRedisTemplate = stringTemplate;

        initMore(connectionFactory);

        // 打上标记，表示已经初始化成功，后续无需再重新初始化
        this.isInit = true;
    }

    protected void initMore(RedisConnectionFactory connectionFactory) {

    }


    /**
     * 获取Value，如无返空
     */
    @Override
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 写入Value，并设定存活时间 (单位: 秒)
     */
    @Override
    public void set(String key, String value, long timeout) {
        if(timeout == 0 || timeout <= SaTokenDao.NOT_VALUE_EXPIRE)  {
            return;
        }
        // 判断是否为永不过期
        if(timeout == SaTokenDao.NEVER_EXPIRE) {
            stringRedisTemplate.opsForValue().set(key, value);
        } else {
            stringRedisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
        }
    }

    /**
     * 修改指定key-value键值对 (过期时间不变)
     */
    @Override
    public void update(String key, String value) {
        @SuppressWarnings("all")
        long expireMs = stringRedisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        // -2 = 无此键
        if (expireMs == SaTokenDao.NOT_VALUE_EXPIRE) {
            return;
        }
        // -1 = 永不过期
        if(expireMs == SaTokenDao.NEVER_EXPIRE) {
            stringRedisTemplate.opsForValue().set(key, value);
        } else {
            stringRedisTemplate.opsForValue().set(key, value, expireMs, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 删除Value
     */
    @Override
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 获取Value的剩余存活时间 (单位: 秒)
     */
    @Override
    public long getTimeout(String key) {
        return stringRedisTemplate.getExpire(key);
    }

    /**
     * 修改Value的剩余存活时间 (单位: 秒)
     */
    @Override
    public void updateTimeout(String key, long timeout) {
        // 判断是否想要设置为永久
        if(timeout == SaTokenDao.NEVER_EXPIRE) {
            long expire = getTimeout(key);
            if(expire == SaTokenDao.NEVER_EXPIRE) {
                // 如果其已经被设置为永久，则不作任何处理
            } else {
                // 如果尚未被设置为永久，那么再次set一次
                this.set(key, this.get(key), timeout);
            }
            return;
        }
        stringRedisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }



    /**
     * 搜索数据
     */
    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        Set<String> keys = stringRedisTemplate.keys(prefix + "*" + keyword + "*");
        List<String> list = new ArrayList<>(keys);
        return SaFoxUtil.searchList(list, start, size, sortType);
    }


}