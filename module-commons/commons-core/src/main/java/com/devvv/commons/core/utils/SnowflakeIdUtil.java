package com.devvv.commons.core.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Pid;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.devvv.commons.core.config.ApplicationInfo;
import com.devvv.commons.core.config.redis.RedisKey;
import com.devvv.commons.core.config.redis.key.SysKeyDefine;
import com.devvv.commons.core.config.redis.template.SysRedisTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Create by WangSJ on 2024/07/11
 * 雪花漂移算法
 * 优势：
 *      生成的id更短
 *      支持漂移: 同一毫秒内生成id达到上限，将会预支下一个毫秒的id
 *      支持瞬时高峰获取id能提高200倍（基于漂移机制）
 *      支持时钟回拨: 每个毫秒都预留了几个序列，用于在时钟回拨时填充值
 *      生成的id在低并发时也能均匀的分布单双数
 * 参考自：
 *      IdGenerator： https://gitee.com/yitter/idgenerator
 *      ShardingJDBC: 在低并发时生成的id能够均匀分布单双数
 */
@Slf4j
public class SnowflakeIdUtil {
    private static final SnowflakeIdGenerator snowflakeIdGenerator;
    static {
        long baseTime = DateUtil.parseDateTime("2025-01-01 00:00:00").getTime();    // 基准时间，功能上线后不可变动
        byte workerIdBitLength = 7;     // workerId的字节长度，为7时可支持2^7-1=127个节点
        short workerId = getWorkerId((1 << workerIdBitLength) - 1); // 工作id，最重要的参数，需保证每个实例都不可重复
        byte seqBitLength = 7;          // 序列数字节长度；2^7-1=127，127-5=122：每毫秒可生成的id数（得益于其漂移预支机制，最大并发能力可达到每个毫秒2.4W个id）
        short minSeqNumber = 5;         // 最小序列数（含）；默认值5，取值范围 [5, MaxSeqNumber]，每毫秒的前5个序列数对应编号是0-4是保留位，其中1-4是时间回拨相应预留位，0是手工新值预留位
        int topOverCostCount = 200;     // 最大漂移次数（含）
        snowflakeIdGenerator = new SnowflakeIdGenerator(baseTime,workerId, workerIdBitLength, seqBitLength, minSeqNumber, topOverCostCount);
    }

    private static short getWorkerId(int maxWorkerId) {
        Short workerId = getWorkerIdByRedis(maxWorkerId);
        if (workerId == null) {
            workerId = (short) RandomUtil.randomInt(maxWorkerId);
            log.warn("[雪花ID]-随机生成初始参数 workerId:{}", workerId);
        } else {
            log.warn("[雪花ID]-通过Redis抢占初始参数 workerId:{}", workerId);
        }
        return workerId;
    }
    private static Short getWorkerIdByRedis(int maxWorkerId) {
        SysRedisTemplate redisTemplate = SpringUtil.getBean(SysRedisTemplate.class);
        if (redisTemplate == null) {
            return null;
        }
        // 初始值随机，降低并发
        int num = ThreadLocalRandom.current().nextInt(maxWorkerId);
        String value = StrUtil.format("{} {}:{} PID:{}", ApplicationInfo.CURRENT_APP_TYPE.name(), ApplicationInfo.SERVER_IP, ApplicationInfo.SERVER_PORT, Pid.INSTANCE.get());
        // 最多循环 MAX_WORKER_ID 次；
        // 从随机的 num 开始，寻找一个位置，抢占id
        for (int i = 0; i < maxWorkerId; i++, num++) {
            int workerId = num % maxWorkerId;
            RedisKey redisKey = RedisKey.create(SysKeyDefine.SnowflakeIdSeq, workerId);
            // 原子性的方式，获取一个key
            Boolean success = redisTemplate.setIfAbsent(redisKey, value);
            // 获取不到，尝试下一个
            if (!success) {
                continue;
            }
            // 启动定时任务，每隔30秒，为这个key续一次有效期
            Thread.ofVirtual().name("SnowflakeIdWatcher").start(() -> {
                while (true) {
                    try {
                        ThreadUtil.sleep(30_000);
                        redisTemplate.expire(redisKey);
                    } catch (Exception e) {
                        log.error("[雪花ID]- WorkerId自动续时异常！ redisKey:{}", redisKey.toKey(), e);
                    }
                }
            });
            return (short) workerId;
        }
        return null;
    }

    /**
     * 获取id
     */
    public static long nextId() {
        return snowflakeIdGenerator.nextId();
    }


    /**
     * 雪花id生成器，参考自: https://github.com/yitter/IdGenerator/blob/master/Java/source/src/main/java/com/github/yitter/core/SnowWorkerM1.java
     */
    private static class SnowflakeIdGenerator {
        /**
         * 基础时间
         */
        protected final long BaseTime;

        /**
         * 机器码
         */
        protected final short WorkerId;

        /**
         * 机器码位长
         */
        protected final byte WorkerIdBitLength;

        /**
         * 自增序列数位长
         */
        protected final byte SeqBitLength;

        /**
         * 最大序列数（含）
         */
        protected final int MaxSeqNumber;

        /**
         * 最小序列数（含）
         */
        protected final short MinSeqNumber;

        /**
         * 最大漂移次数
         */
        protected final int TopOverCostCount;

        protected final byte _TimestampShift;
        protected final static byte[] _SyncLock = new byte[0];

        private byte sequenceOffset;            // 在每个新的毫秒时，动态变更成0/1为了在低并发时，生成的id能够均匀分布单双数
        protected short _CurrentSeqNumber;
        protected long _LastTimeTick = 0;
        protected long _TurnBackTimeTick = 0;   // 时钟回拨，在生成id时，往前偏移的毫秒数
        protected byte _TurnBackIndex = 0;


        /** 本周期id已达到上限，是否开始预支下个周期的id */
        private boolean _IsOverCost = false;
        /** 连续预支，往后漂移的周期数 */
        private int _OverCostCountInOneTerm = 0;
        /** 连续预支，总用生成的id数 */
        private int _GenCountInOneTerm = 0;
        private int _TermIndex = 0;              // 累计 发生了几次预支

        public SnowflakeIdGenerator(long baseTime, short workerId, byte workerIdBitLength, byte seqBitLength, short minSeqNumber, int topOverCostCount) {
            this.BaseTime = baseTime;
            this.WorkerIdBitLength = workerIdBitLength;
            this.WorkerId = workerId;
            this.SeqBitLength = seqBitLength;
            this.MaxSeqNumber = (1 << seqBitLength) - 1;
            this.MinSeqNumber = minSeqNumber;
            this.TopOverCostCount = topOverCostCount;
            this._TimestampShift = (byte) (workerIdBitLength + seqBitLength);
            this._CurrentSeqNumber = GetInitSeqNumber();
        }

        private void BeginOverCostAction(long useTimeTick) {}

        private void EndOverCostAction(long useTimeTick) {
            if (_TermIndex > 10000) {
                _TermIndex = 0;
            }
        }

        private void BeginTurnBackAction(long useTimeTick) {}

        private void EndTurnBackAction(long useTimeTick) {}

        /**
         * 预支id（漂移）
         */
        private long NextOverCostId() {
            long currentTimeTick = GetCurrentTimeTick();

            // 追平，新的周期开始
            if (currentTimeTick > _LastTimeTick) {
                EndOverCostAction(currentTimeTick);

                _LastTimeTick = currentTimeTick;
                _CurrentSeqNumber = GetInitSeqNumber();
                _IsOverCost = false;
                _OverCostCountInOneTerm = 0;
                _GenCountInOneTerm = 0;

                return CalcId(_LastTimeTick);
            }

            // 达到了预支周期上限
            if (_OverCostCountInOneTerm >= TopOverCostCount) {
                EndOverCostAction(currentTimeTick);

                _LastTimeTick = GetNextTimeTick();      // 等待至下一个周期
                _CurrentSeqNumber = GetInitSeqNumber();
                _IsOverCost = false;
                _OverCostCountInOneTerm = 0;
                _GenCountInOneTerm = 0;

                return CalcId(_LastTimeTick);
            }

            // 达到了id上限，继续漂移到下下个周期
            if (_CurrentSeqNumber > MaxSeqNumber) {
                _LastTimeTick++;
                _CurrentSeqNumber = GetInitSeqNumber();
                _IsOverCost = true;
                _OverCostCountInOneTerm++;
                _GenCountInOneTerm++;

                return CalcId(_LastTimeTick);
            }

            _GenCountInOneTerm++;
            return CalcId(_LastTimeTick);
        }

        private long NextNormalId() {
            long currentTimeTick = GetCurrentTimeTick();

            // 发生了时钟回拨
            if (currentTimeTick < _LastTimeTick) {
                if (_TurnBackTimeTick < 1) {        // 设置时钟回拨的标记-起始毫秒
                    _TurnBackTimeTick = _LastTimeTick - 1;
                    _TurnBackIndex++;

                    // 每毫秒序列数的前5位是预留位，0用于手工新值，1-4是时间回拨次序
                    // 最多4次回拨（防止回拨重叠）
                    if (_TurnBackIndex > 4) {
                        _TurnBackIndex = 1;
                    }
                    BeginTurnBackAction(_TurnBackTimeTick);
                }
                // 处于时钟回拨期间，每次生成id时，都将前退1毫秒再根据index生成id
                // 每经过4个回拨期间，重置index
                return CalcTurnBackId(_TurnBackTimeTick);
            }

            // 时间追平时，_TurnBackTimeTick清零
            if (_TurnBackTimeTick > 0) {
                EndTurnBackAction(_TurnBackTimeTick);
                _TurnBackTimeTick = 0;
            }

            // 新的1毫秒开始
            if (currentTimeTick > _LastTimeTick) {
                _LastTimeTick = currentTimeTick;
                _CurrentSeqNumber = GetInitSeqNumber();

                return CalcId(_LastTimeTick);
            }

            // 同一毫秒内，id数达到了上限
            if (_CurrentSeqNumber > MaxSeqNumber) {
                BeginOverCostAction(currentTimeTick);

                _TermIndex++;
                _LastTimeTick++;                    // 时间推进到下一个周期
                _CurrentSeqNumber = GetInitSeqNumber();
                _IsOverCost = true;                 // 标记 开始预支id
                _OverCostCountInOneTerm = 1;        // 预支id数
                _GenCountInOneTerm = 1;             // 总数
                // 返回下一个周期的id
                return CalcId(_LastTimeTick);
            }

            // 同一毫秒内，正常生成id
            return CalcId(_LastTimeTick);
        }

        /**
         * 2024-07-11 12:20:34  WangSJ
         * 在每个新的毫秒时，动态增加0/1，为了在低并发时生成的id能够均匀分布单双数
         */
        private short GetInitSeqNumber() {
            sequenceOffset = (byte) (~sequenceOffset & 1);
            return (short) (MinSeqNumber + sequenceOffset);
        }

        private long CalcId(long useTimeTick) {
            long result = ((useTimeTick << _TimestampShift) + ((long) WorkerId << SeqBitLength) + (int) _CurrentSeqNumber);
            _CurrentSeqNumber++;
            return result;
        }

        private long CalcTurnBackId(long useTimeTick) {
            long result = ((useTimeTick << _TimestampShift) + ((long) WorkerId << SeqBitLength) + _TurnBackIndex);
            _TurnBackTimeTick--;
            return result;
        }

        protected long GetCurrentTimeTick() {
            long millis = System.currentTimeMillis();
            return millis - BaseTime;
        }

        /**
         * 等待至下一个周期
         */
        protected long GetNextTimeTick() {
            long tempTimeTicker = GetCurrentTimeTick();
            while (tempTimeTicker <= _LastTimeTick) {
                tempTimeTicker = GetCurrentTimeTick();
            }
            return tempTimeTicker;
        }

        public long nextId() {
            synchronized (_SyncLock) {
                return _IsOverCost ? NextOverCostId() : NextNormalId();
            }
        }
    }
}
