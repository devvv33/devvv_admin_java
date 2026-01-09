package com.devvv.commons.core.config.redis.template;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.devvv.commons.core.config.redis.RedisKey;
import com.devvv.commons.core.config.redis.key.KeyDefine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.GeoShape;

import java.lang.reflect.Type;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis操作封装
 * <p>
 * 目前只有以下命令支持自动设置有效期
 * 1、set
 * 2、setObject
 * 3、incr
 * 4、incrby
 * 5、decr
 * 6、decrby
 * <p>
 * 其他命令需在合适时机调用expire设置有效期
 */
@Slf4j
public abstract class AbstractRedisTemplate {

    protected final RedisTemplate<String,String> redisTemplate;
    protected AbstractRedisTemplate(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 声明支持的key定义
     */
    public abstract Class<? extends KeyDefine> useRedisKeyDefinition();


    public String get(RedisKey redisKey) {
        Object value = redisTemplate.boundValueOps(redisKey.toKey()).get();
        return value == null ? null : value.toString();
    }

    /**
     * 获取旧值，然后删除
     */
    public String getAndDelete(RedisKey redisKey) {
        Object value = redisTemplate.boundValueOps(redisKey.toKey()).getAndDelete();
        return value == null ? null : value.toString();
    }

    /**
     * 获取旧值，然后设置过期时间
     */
    public String getAndExpire(RedisKey redisKey) {
        Object value = redisTemplate.boundValueOps(redisKey.toKey()).getAndExpire(Duration.ofSeconds(redisKey.toSeconds()));
        return value == null ? null : value.toString();
    }

    /**
     * 返回旧值，设置新值
     */
    public String getAndSet(RedisKey redisKey,String value) {
        Object oldValue = redisTemplate.boundValueOps(redisKey.toKey()).getAndSet(value);
        return oldValue == null ? null : oldValue.toString();
    }


    public Integer getInteger(RedisKey redisKey) {
        String value = get(redisKey);
        return value == null ? null : Integer.parseInt(value);
    }
    public int getIntValue(RedisKey redisKey) {
        String value = get(redisKey);
        return value == null ? 0 : Integer.parseInt(value);
    }

    public Long getLong(RedisKey redisKey) {
        String value = get(redisKey);
        return value == null ? null : Long.parseLong(value);
    }
    public long getLongValue(RedisKey redisKey) {
        String value = get(redisKey);
        return value == null ? 0L : Long.parseLong(value);
    }

    public <T> T getObject(RedisKey redisKey, Type type) {
        String value = get(redisKey);
        if (StrUtil.isBlank(value)) {
            return null;
        }
        return JSONObject.parseObject(value, type);
    }

    public void set(RedisKey redisKey, String value) {
        set(redisKey, value,
                (ops, val) -> {
                    ops.set(val);
                    return null;
                },
                (ops, val, seconds) -> {
                    ops.set(val, seconds);
                    return null;
                });
    }

    /**
     * 当key不存在时，进行设置
     * @param redisKey
     * @param value
     * @return
     */
    public Boolean setIfAbsent(RedisKey redisKey,String value){
        return set(redisKey, value, (ops, val) -> ops.setIfAbsent(val), (ops, val, seconds) -> ops.setIfAbsent(val, seconds));
    }

    /**
     * 自增，并返回自增后的值
     * 首次调用返回1
     */
    public Long incrementAndGet(RedisKey redisKey) {
        return incrementAndGet(redisKey, 1);
    }
    public Long incrementAndGet(RedisKey redisKey, int step) {
        String key = redisKey.toKey();
        Integer seconds;
        BoundValueOperations<String, String> ops = redisTemplate.boundValueOps(key);
        Long increment = ops.increment(step);
        if (increment != null && increment == step && (seconds = redisKey.toSeconds()) != null && seconds >= 0) {
            ops.expire(Duration.ofSeconds(seconds));
        }
        return increment;
    }


    /**
     * 自减，并返回自减后的值
     * 首次调用返回-1
     */
    public Long decrementAndGet(RedisKey redisKey) {
        return decrementAndGet(redisKey, 1);
    }
    public Long decrementAndGet(RedisKey redisKey,int step) {
        String key = redisKey.toKey();
        Integer seconds;
        BoundValueOperations<String, String> ops = redisTemplate.boundValueOps(key);
        Long increment = ops.decrement(step);
        if (increment != null && increment == -step && (seconds = redisKey.toSeconds()) != null && seconds >= 0) {
            ops.expire(Duration.ofSeconds(seconds));
        }
        return increment;
    }

    /**
     * 只当key存在时，进行更新
     */
    public Boolean setIfPresent(RedisKey redisKey,String value){
        return set(redisKey, value, (ops, val) -> ops.setIfPresent(val), (ops, val, seconds) -> ops.setIfPresent(val, seconds));
    }

    private Boolean set(RedisKey redisKey, String value, SetValue setValue,SetValueWithTime setValueWithTime){
        String key = redisKey.toKey();
        Integer seconds = redisKey.toSeconds();
        if (seconds == null || seconds == -1) {
            return setValue.callback(redisTemplate.boundValueOps(key), value);
        } else if (seconds == 0) {
            redisTemplate.delete(redisKey.toKey());
            return true;
        } else {
            return setValueWithTime.callback(redisTemplate.boundValueOps(key), value, Duration.ofSeconds(seconds));
        }
    }


    public interface SetValue{
        Boolean callback(BoundValueOperations opt, String value);
    }
    public interface SetValueWithTime{
        Boolean callback(BoundValueOperations opt, String value, Duration seconds);
    }

    public Boolean delete(RedisKey redisKey) {
        return redisTemplate.delete(redisKey.toKey());
    }
    public Long delete(RedisKey... redisKeys) {
        List<String> keys = new ArrayList<>(redisKeys.length);
        for (RedisKey k : redisKeys) {
            keys.add(k.toKey());
        }
        return redisTemplate.delete(keys);
    }

    public Boolean unlink(RedisKey redisKey) {
        return redisTemplate.unlink(redisKey.toKey());
    }
    public Long unlink(RedisKey... redisKeys) {
        List<String> keys = new ArrayList<>(redisKeys.length);
        for (RedisKey k : redisKeys) {
            keys.add(k.toKey());
        }
        return redisTemplate.unlink(keys);
    }




    // region 其他操作
    public Boolean exists(RedisKey redisKey) {
        return redisTemplate.hasKey(redisKey.toKey());
    }

    public Boolean expire(RedisKey redisKey) {
        Integer seconds = redisKey.toSeconds();
        if (seconds == null || seconds == 0) {
            return false;
        }
        return redisTemplate.expire(redisKey.toKey(), redisKey.toSeconds(), TimeUnit.SECONDS);
    }
    public Boolean expireAt(RedisKey redisKey, Date date) {
        return redisTemplate.expireAt(redisKey.toKey(), date);
    }
    // 获取过期时间，秒
    public Long getExpire(RedisKey redisKey) {
        return redisTemplate.getExpire(redisKey.toKey());
    }
    // 获取基础操作类
    public BoundValueOperations<String, String> opsString(RedisKey redisKey) {
        return redisTemplate.boundValueOps(redisKey.toKey());
    }
    // endregion



    // region list操作
    public BoundListOperations<String, String> opsList(RedisKey redisKey) {
        return redisTemplate.boundListOps(redisKey.toKey());
    }
    // 查询列表长度
    public Long lSize(RedisKey redisKey) {
        return redisTemplate.opsForList().size(redisKey.toKey());
    }
    // 范围查询，start:从0开始，包含  end:包含  边界可以为负数
    public List<String> lRange(RedisKey redisKey, long start, long end) {
        return redisTemplate.opsForList().range(redisKey.toKey(), start, end);
    }
    // 修剪列表，仅保留其范围内的列表  start:从0开始，包含  end:包含  边界可以为负数
    public void lTrim(RedisKey redisKey, long start, long end) {
        redisTemplate.opsForList().trim(redisKey.toKey(), start, end);
    }
    // 向指定索引位置添加元素
    public void lSet(RedisKey redisKey, long index, String value) {
        redisTemplate.opsForList().set(redisKey.toKey(), index, value);
    }
    // 获取指定索引的元素
    public String lIndex(RedisKey redisKey, long index) {
        return redisTemplate.opsForList().index(redisKey.toKey(), index);
    }
    // 删除元素
    //  count>0 从左侧，删除count个value
    //  count<0 从右侧，删除count个value
    //  count=0 删除所有value
    // 删除所有value
    public void lRemove(RedisKey redisKey, String value) {
        redisTemplate.opsForList().remove(redisKey.toKey(), 0, value);
    }
    // 从左侧开始，删除count个value
    public void lRemove(RedisKey redisKey, long count, String value) {
        redisTemplate.opsForList().remove(redisKey.toKey(), Math.abs(count), value);
    }
    // 从右侧开始，删除count个value
    public void rRemove(RedisKey redisKey, long count, String value) {
        redisTemplate.opsForList().remove(redisKey.toKey(), -Math.abs(count), value);
    }
    // 向左侧添加元素
    public Long lPush(RedisKey redisKey, String... values) {
        Assert.isTrue(values.length > 0, "参数不能为空");
        Long l = redisTemplate.opsForList().leftPushAll(redisKey.toKey(), values);
        if (l != null && l == values.length) {
            expire(redisKey);
        }
        return l;
    }
    // 从左侧弹出元素
    public String lPop(RedisKey redisKey) {
        return redisTemplate.opsForList().leftPop(redisKey.toKey());
    }
    public List<String> lPop(RedisKey redisKey, long count) {
        return redisTemplate.opsForList().leftPop(redisKey.toKey(), count);
    }
    public String blPop(RedisKey redisKey, long second) {
        return redisTemplate.opsForList().leftPop(redisKey.toKey(), second, TimeUnit.SECONDS);
    }

    // 向右侧添加元素
    public Long rPush(RedisKey redisKey, String... values) {
        Assert.isTrue(values.length > 0, "参数不能为空");
        Long l = redisTemplate.opsForList().rightPushAll(redisKey.toKey(), values);
        if (l != null && l == values.length) {
            expire(redisKey);
        }
        return l;
    }
    // 从右侧弹出元素
    public String rPop(RedisKey redisKey) {
        return redisTemplate.opsForList().rightPop(redisKey.toKey());
    }
    public List<String> rPop(RedisKey redisKey, long count) {
        return redisTemplate.opsForList().rightPop(redisKey.toKey(), count);
    }
    public String brPop(RedisKey redisKey, long second) {
        return redisTemplate.opsForList().rightPop(redisKey.toKey(), second, TimeUnit.SECONDS);
    }
    // endregion


    // region set操作
    public BoundSetOperations<String, String> opsSet(RedisKey redisKey){
        return redisTemplate.boundSetOps(redisKey.toKey());
    }
    // 返回插入成功的个数
    public Long sAdd(RedisKey redisKey, String... values) {
        Assert.isTrue(values.length > 0, "参数不能为空");
        return redisTemplate.opsForSet().add(redisKey.toKey(), values);
    }
    // 返回删除成功的个数
    public Long sRemove(RedisKey redisKey, Object... values) {
        Assert.isTrue(values.length > 0, "参数不能为空");
        return redisTemplate.opsForSet().remove(redisKey.toKey(), values);
    }
    // 从set集合中，随机弹出一个元素
    public String sPop(RedisKey redisKey) {
        return redisTemplate.opsForSet().pop(redisKey.toKey());
    }
    public List<String> sPop(RedisKey redisKey, long count) {
        return redisTemplate.opsForSet().pop(redisKey.toKey(), count);
    }
    // 从set集合中随机获取元素，不删除
    public String sRandomMember(RedisKey redisKey) {
        return redisTemplate.opsForSet().randomMember(redisKey.toKey());
    }
    public Set<String> sRandomMember(RedisKey redisKey, long count) {
        return redisTemplate.opsForSet().distinctRandomMembers(redisKey.toKey(), count);
    }
    public Set<String> sMembers(RedisKey redisKey){
        return redisTemplate.opsForSet().members(redisKey.toKey());
    }
    public Long sSize(RedisKey redisKey) {
        return redisTemplate.opsForSet().size(redisKey.toKey());
    }
    public Boolean sIsMember(RedisKey redisKey, String value) {
        return redisTemplate.opsForSet().isMember(redisKey.toKey(), value);
    }
    // endregion


    // region hash操作
    public BoundHashOperations<String, String, String> opsHash(RedisKey redisKey) {
        return redisTemplate.boundHashOps(redisKey.toKey());
    }
    public void hSet(RedisKey redisKey, String field, String value) {
        redisTemplate.opsForHash().put(redisKey.toKey(), field, value);
    }
    public void hMSet(RedisKey redisKey, Map<String, String> keyValues) {
        redisTemplate.opsForHash().putAll(redisKey.toKey(), keyValues);
    }
    // 仅当field不存在时才设置
    public Boolean hSetIfAbsent(RedisKey redisKey, String field, String value) {
        return redisTemplate.opsForHash().putIfAbsent(redisKey.toKey(), field, value);
    }
    public String hGet(RedisKey redisKey, String field) {
        Object val = redisTemplate.opsForHash().get(redisKey.toKey(), field);
        return val == null ? null : val.toString();
    }
    public List<String> hMGet(RedisKey redisKey, String... fields) {
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        return ops.multiGet(redisKey.toKey(), Arrays.asList(fields));
    }
    public Map<String, String> hGetAll(RedisKey redisKey) {
        HashOperations<String, String, String> ops = redisTemplate.opsForHash();
        return ops.entries(redisKey.toKey());
    }
    public Long hDelete(RedisKey redisKey, String... fields) {
        Assert.isTrue(fields.length > 0, "参数不能为空");
        return redisTemplate.opsForHash().delete(redisKey.toKey(), fields);
    }
    public Boolean hExists(RedisKey redisKey, String field) {
        return redisTemplate.opsForHash().hasKey(redisKey.toKey(), field);
    }
    public Long hSize(RedisKey redisKey) {
        return redisTemplate.opsForHash().size(redisKey.toKey());
    }
    // 字段自增，返回自增后的值
    public Long hIncrement(RedisKey redisKey, String field, long increment) {
        return redisTemplate.opsForHash().increment(redisKey.toKey(), field, increment);
    }
    public Integer hGetInt(RedisKey redisKey, String field) {
        return Opt.ofBlankAble(hGet(redisKey, field))
                .map(Integer::valueOf)
                .orElse(null);
    }
    public int hGetIntValue(RedisKey redisKey, String field) {
        return Opt.ofBlankAble(hGet(redisKey, field))
                .map(Integer::valueOf)
                .orElse(0);
    }
    public Long hGetLong(RedisKey redisKey, String field) {
        return Opt.ofBlankAble(hGet(redisKey, field))
                .map(Long::valueOf)
                .orElse(null);
    }
    public long hGetLongValue(RedisKey redisKey, String field) {
        return Opt.ofBlankAble(hGet(redisKey, field))
                .map(Long::valueOf)
                .orElse(0L);
    }
    public <T> T hGetObject(RedisKey redisKey, String field, Class<T> clazz) {
        return Opt.ofBlankAble(hGet(redisKey, field))
                .map(str -> JSONObject.parseObject(str, clazz))
                .orElse(null);
    }
    // endregion


    // region zset操作
    public BoundZSetOperations<String, String> opsZSet(RedisKey redisKey) {
        return redisTemplate.boundZSetOps(redisKey.toKey());
    }
    public Boolean zAdd(RedisKey redisKey, String member, double score) {
        return redisTemplate.opsForZSet().add(redisKey.toKey(), member, score);
    }
    public Long zAdd(RedisKey redisKey, Map<String, Double> members) {
        Set<ZSetOperations.TypedTuple<String>> tuples = members.entrySet().stream()
                .map(entry -> ZSetOperations.TypedTuple.of(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
        return redisTemplate.opsForZSet().add(redisKey.toKey(), tuples);
    }
    public Boolean zAddIfAbsent(RedisKey redisKey, String member, double score) {
        return redisTemplate.opsForZSet().addIfAbsent(redisKey.toKey(), member, score);
    }
    public Long zAddIfAbsent(RedisKey redisKey, Map<String, Double> members) {
        Set<ZSetOperations.TypedTuple<String>> tuples = members.entrySet().stream()
                .map(entry -> ZSetOperations.TypedTuple.of(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
        return redisTemplate.opsForZSet().addIfAbsent(redisKey.toKey(), tuples);
    }
    public Boolean zExists(RedisKey redisKey, String member) {
        return zScore(redisKey, member) != null;
    }
    public Long zSize(RedisKey redisKey) {
        return redisTemplate.opsForZSet().zCard(redisKey.toKey());
    }
    public Long zCount(RedisKey redisKey, double min, double max) {
        return redisTemplate.opsForZSet().count(redisKey.toKey(), min, max);
    }

    public Long zRemove(RedisKey redisKey, Object... members) {
        return redisTemplate.opsForZSet().remove(redisKey.toKey(), members);
    }
    // 根据排名，从低到高 删除元素，排名可以为负数
    public Long zRemoveByRank(RedisKey redisKey, long start, long end) {
        return redisTemplate.opsForZSet().removeRange(redisKey.toKey(), start, end);
    }
    // 根据分数，删除元素
    public Long zRemoveByScore(RedisKey redisKey, double min, double max) {
        return redisTemplate.opsForZSet().removeRangeByScore(redisKey.toKey(), min, max);
    }

    // 随机获取元素
    public String zRandomMember(RedisKey redisKey) {
        return redisTemplate.opsForZSet().randomMember(redisKey.toKey());
    }
    public Set<String> zRandomMember(RedisKey redisKey, long count) {
        return redisTemplate.opsForZSet().distinctRandomMembers(redisKey.toKey(), count);
    }

    // 返回 元素从低到高的排名，从0开始
    public Long zRank(RedisKey redisKey, Object member) {
        return redisTemplate.opsForZSet().rank(redisKey.toKey(), member);
    }
    // 返回 元素从高到低的排名，从0开始
    public Long zReverseRank(RedisKey redisKey, Object member) {
        return redisTemplate.opsForZSet().reverseRank(redisKey.toKey(), member);
    }
    // 获取元素分数
    public Double zScore(RedisKey redisKey, String member) {
        return redisTemplate.opsForZSet().score(redisKey.toKey(), member);
    }
    public List<Double> zScores(RedisKey redisKey, Object... member) {
        return redisTemplate.opsForZSet().score(redisKey.toKey(), member);
    }

    // 从小到大，根据 排名 获取元素；
    // start:从0开始，可以为负数，包含
    // end:从0开始，可以为负数，包含
    public Set<String> zRange(RedisKey redisKey, long start, long end) {
        return redisTemplate.opsForZSet().range(redisKey.toKey(), start, end);
    }
    public Set<ZSetOperations.TypedTuple<String>> zRangeWithScores(RedisKey redisKey, long start, long end) {
        return redisTemplate.opsForZSet().rangeWithScores(redisKey.toKey(), start, end);
    }
    public Set<String> zReverseRange(RedisKey redisKey, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(redisKey.toKey(), start, end);
    }
    public Set<ZSetOperations.TypedTuple<String>> zReverseRangeWithScores(RedisKey redisKey, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(redisKey.toKey(), start, end);
    }

    // 从小到大，根据 分数 获取元素；
    // min:分数，包含
    // max:分数，包含
    // offset: 从0开始，包含
    public Set<String> zRangeByScore(RedisKey redisKey, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(redisKey.toKey(), min, max);
    }
    public Set<String> zRangeByScore(RedisKey redisKey, double min, double max, long offset, long count) {
        return redisTemplate.opsForZSet().rangeByScore(redisKey.toKey(), min, max, offset, count);
    }
    public Set<ZSetOperations.TypedTuple<String>> zRangeByScoreWithScores(RedisKey redisKey, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(redisKey.toKey(), min, max);
    }
    public Set<ZSetOperations.TypedTuple<String>> zRangeByScoreWithScores(RedisKey redisKey, double min, double max, long offset, long count) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(redisKey.toKey(), min, max, offset, count);
    }
    public Set<String> zReverseRangeByScore(RedisKey redisKey, double min, double max) {
        return redisTemplate.opsForZSet().reverseRangeByScore(redisKey.toKey(), min, max);
    }
    public Set<String> zReverseRangeByScore(RedisKey redisKey, double min, double max, long offset, long count) {
        return redisTemplate.opsForZSet().reverseRangeByScore(redisKey.toKey(), min, max, offset, count);
    }
    public Set<ZSetOperations.TypedTuple<String>> zReverseRangeByScoreWithScores(RedisKey redisKey, double min, double max) {
        return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(redisKey.toKey(), min, max);
    }
    public Set<ZSetOperations.TypedTuple<String>> zReverseRangeByScoreWithScores(RedisKey redisKey, double min, double max, long offset, long count) {
        return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(redisKey.toKey(), min, max, offset, count);
    }

    // 弹出元素
    // 弹出分数最小的
    public ZSetOperations.TypedTuple<String> zPopMin(RedisKey redisKey) {
        return redisTemplate.opsForZSet().popMin(redisKey.toKey());
    }
    public Set<ZSetOperations.TypedTuple<String>> zPopMin(RedisKey redisKey, long count) {
        return redisTemplate.opsForZSet().popMin(redisKey.toKey(), count);
    }
    public ZSetOperations.TypedTuple<String> bzPopMin(RedisKey redisKey, long timeoutSecond) {
        return redisTemplate.opsForZSet().popMin(redisKey.toKey(), timeoutSecond, TimeUnit.SECONDS);
    }
    // 弹出分数最大的
    public ZSetOperations.TypedTuple<String> zPopMax(RedisKey redisKey) {
        return redisTemplate.opsForZSet().popMax(redisKey.toKey());
    }
    public Set<ZSetOperations.TypedTuple<String>> zPopMax(RedisKey redisKey, long count) {
        return redisTemplate.opsForZSet().popMax(redisKey.toKey(), count);
    }
    public ZSetOperations.TypedTuple<String> bzPopMax(RedisKey redisKey, long timeoutSecond) {
        return redisTemplate.opsForZSet().popMax(redisKey.toKey(), timeoutSecond, TimeUnit.SECONDS);
    }
    // endregion


    // region Geo操作
    public BoundGeoOperations<String, String> opsGeo(RedisKey redisKey) {
        return redisTemplate.boundGeoOps(redisKey.toKey());
    }
    public void geoAdd(RedisKey redisKey, String member, double x, double y) {
        redisTemplate.opsForGeo().add(redisKey.toKey(), new Point(x, y), member);
    }
    public void geoAdd(RedisKey redisKey, Map<String, Point> map) {
        redisTemplate.opsForGeo().add(redisKey.toKey(), map);
    }
    public void geoRemove(RedisKey redisKey, String... members) {
        redisTemplate.opsForGeo().remove(redisKey.toKey(), members);
    }
    public Long geoSize(RedisKey redisKey) {
        return opsZSet(redisKey).size();
    }
    // 获取元素位置
    public List<Point> geoPosition(RedisKey redisKey, String... members) {
        return redisTemplate.opsForGeo().position(redisKey.toKey(), members);
    }
    // 计算两点之间的距离
    public Distance geoDistance(RedisKey redisKey, String member1, String member2, Metric metric) {
        return redisTemplate.opsForGeo().distance(redisKey.toKey(), member1, member2, metric);
    }

    // 以中心点+半径 画一个圆，返回圆圈覆盖到的所有元素
    public GeoResults<RedisGeoCommands.GeoLocation<String>> geoRadius(RedisKey redisKey, Point center, Distance radius) {
        return redisTemplate.opsForGeo().radius(redisKey.toKey(), new Circle(center, radius));
    }
    // args: 额外参数：
    //     includeCoordinates： 返回结果包含坐标
    //     includeDistance:     返回结果包含距离
    //     sortAscending:   相对于中心，由近到远排序
    //     sortDescending:  相对于中心，由远到近排序
    //     limit：   限定返回几条数据
    public GeoResults<RedisGeoCommands.GeoLocation<String>> geoRadius(RedisKey redisKey, Point center, Distance radius, RedisGeoCommands.GeoRadiusCommandArgs args) {
        return redisTemplate.opsForGeo().radius(redisKey.toKey(), new Circle(center, radius), args);
    }
    // 以成员为中心点+半径 画一个圆，返回圆圈覆盖到的所有元素
    public GeoResults<RedisGeoCommands.GeoLocation<String>> geoRadius(RedisKey redisKey, String member, Distance radius) {
        return redisTemplate.opsForGeo().radius(redisKey.toKey(), member, radius);
    }
    public GeoResults<RedisGeoCommands.GeoLocation<String>> geoRadius(RedisKey redisKey, String member, Distance radius, RedisGeoCommands.GeoRadiusCommandArgs args) {
        return redisTemplate.opsForGeo().radius(redisKey.toKey(), member, radius, args);
    }

    // search查询，6.2版本才支持
    // 可按照圆形或矩形查询
    public GeoResults<RedisGeoCommands.GeoLocation<String>> geoSearch(RedisKey redisKey, GeoReference<String> reference, GeoShape shape) {
        return redisTemplate.opsForGeo().search(redisKey.toKey(), reference, shape, RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs());
    }
    public GeoResults<RedisGeoCommands.GeoLocation<String>> geoSearch(RedisKey redisKey, GeoReference<String> reference, GeoShape shape, RedisGeoCommands.GeoSearchCommandArgs args) {
        return redisTemplate.opsForGeo().search(redisKey.toKey(), reference, shape, args);
    }
    // endregion

    // region Stream操作
    public BoundStreamOperations<String, String, String> opsStream(RedisKey redisKey) {
        return redisTemplate.boundStreamOps(redisKey.toKey());
    }

    // 发送消息
    public RecordId xSend(RedisKey redisKey, Map<String, String> body) {
        return redisTemplate.opsForStream().add(redisKey.toKey(), body);
    }

    // 删除消息
    public Long xDelete(RedisKey redisKey, String... recordIds) {
        return redisTemplate.opsForStream().delete(redisKey.toKey(), recordIds);
    }
    // 读取消息
    public void xConsumer(RedisKey redisKey, String consumerGroup, java.util.function.Consumer<Map<String, String>> consumer) {
        BoundStreamOperations<String, String, String> opsStream = opsStream(redisKey);

        // 确认消费者组存在，不存在则创建
        try {
            opsStream.createGroup(ReadOffset.latest(), consumerGroup);
        } catch (Exception e) {
            // 消费组已存在
        }

        Consumer consumerInfo = Consumer.from(consumerGroup, "Simple");
        // 定义消息读取选项
        StreamReadOptions readOptions = StreamReadOptions.empty()
                .block(Duration.ofSeconds(60)) // 阻塞时间
                .count(5); // 每次读取的最大消息数

        // 开启子线程，循环处理
        Thread.ofVirtual()
                .name("RedisXConsumer-" + consumerGroup)
                .uncaughtExceptionHandler((t, e) -> log.error("Redis-Stream消费处理失败！", e))
                .start(() -> {
                    while (true) {
                        List<MapRecord<String, String, String>> messagelist = opsStream.read(readOptions, ReadOffset.lastConsumed());
                        if (messagelist != null && !messagelist.isEmpty()) {
                            // 读取到消息，处理消息
                            for (MapRecord<String, String, String> message : messagelist) {
                                String key = message.getStream();
                                Map<String, String> body = message.getValue();
                                try {
                                    consumer.accept(body);
                                    // 确认消息已被处理
                                    opsStream.acknowledge(consumerGroup, message.getId().getValue());
                                } catch (Exception e) {
                                    log.error("Redis-Stream消费处理失败！ Key:{} Message:{}", key, JSONObject.toJSONString(body), e);
                                }
                            }
                        }
                    }
                });
    }
    // endregion


    /**
     * 在管道中批量执行操作
     * 每个命令仍是独立的，如果中间失败，那么前边执行的也不会回滚
     */
    public List<Object> execPipelined(RedisCallback<?> action){
        return redisTemplate.executePipelined(action);
    }

    /**
     * 在Redis的事务中执行操作
     */
    @SuppressWarnings("unchecked")
    public List<Object> execTransaction(java.util.function.Consumer<RedisOperations> action){
        return redisTemplate.execute(new SessionCallback<>() {
            @Override
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                // 开启Redis事务
                operations.multi();
                action.accept(operations);
                // 此时命令还没有被执行，而是加入到队列中
                return operations.exec();  // 执行事务
            }
        });
    }

    /**
     * 执行lua脚本
     */
    public <T> T evalScript(String script, Class<T> resultType, List<String> keys, String... args) {
        DefaultRedisScript<T> sc = new DefaultRedisScript<>();
        sc.setScriptText(script);
        sc.setResultType(resultType);
        return redisTemplate.execute(sc, keys, args);
    }
}
