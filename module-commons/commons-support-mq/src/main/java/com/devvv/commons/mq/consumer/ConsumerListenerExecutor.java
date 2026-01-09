package com.devvv.commons.mq.consumer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.devvv.commons.core.busicode.BusiCodeDefine;
import com.devvv.commons.core.config.redis.RedisKey;
import com.devvv.commons.core.config.redis.key.LimitKeyDefine;
import com.devvv.commons.core.config.redis.template.LimitRedisTemplate;
import com.devvv.commons.core.context.BusiContext;
import com.devvv.commons.core.context.BusiContextHolder;
import com.devvv.commons.core.context.BusiContextUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * Create by WangSJ on 2024/07/01
 */
@Slf4j
public abstract class ConsumerListenerExecutor implements MessageListenerConcurrently {

    @Resource
    private LimitRedisTemplate limitRedisTemplate;

    /**
     * 消费消息
     */
    public abstract void doConsume(BusiContext context);


    public void doProcess(String title, Runnable callback) {
        try {
            long startTime = System.currentTimeMillis();
            callback.run();
            long execTime = System.currentTimeMillis() - startTime;
            if (execTime >= 5_000) {
                log.warn("[MQ-Consumer]- 业务:[{}] 执行耗时过长！ 用时:{}", title, DateUtil.formatBetween(execTime));
            }
        } catch (Exception e) {
            log.error("[MQ-Consumer]- 业务:[{}] 执行失败！ context:{}", title, JSONObject.toJSONString(BusiContextHolder.getContext()), e);
        }
    }

    public void doProcess(BusiCodeDefine busiCode, String title, Runnable callback) {
        if (BusiContextUtil.getContext().getBusiCode() != busiCode) {
            return;
        }
        doProcess(title, callback);
    }

    /**
     * 获取当前类注解上的groupId
     */
    private String groupId;
    private String getGroupId() {
        if (StrUtil.isBlank(groupId)) {
            synchronized (this) {
                if (StrUtil.isBlank(groupId)) {
                    this.groupId = Opt.ofNullable(this.getClass().getAnnotation(ConsumerListener.class))
                            .map(ConsumerListener::groupId)
                            .map(groupId -> StrUtil.blankToDefault(groupId, "null"))
                            .orElse("null");
                }
            }
        }
        return groupId;
    }

    @Override
    public final ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        for (MessageExt msg : msgs) {
            // 当前消费组，重复消费检查
            if (!limitRedisTemplate.setIfAbsent(RedisKey.create(LimitKeyDefine.MQ, getGroupId(), msg.getKeys()), msg.getTags())) {
                log.warn("[MQ-Consumer]- 重复消费-跳过处理 {} GroupId:{} Kyes:{} Tags:{} \n  {}", this.getClass().getName(), getGroupId(), msg.getKeys(), msg.getTags(), new String(msg.getBody()));
                continue;
            }

            String jsonStr = null;
            try {
                long times = System.currentTimeMillis() - msg.getBornTimestamp();
                if (times > 30_000) {
                    log.warn("[MQ-Consumer]- 消息发生堆积，消息延迟:{} Topic:{} Tags:{}", DateUtil.formatBetween(times), msg.getTopic(), msg.getTags());
                }

                jsonStr = new String(msg.getBody());
                BusiContext busiContext = JSONObject.parseObject(jsonStr, BusiContext.class);
                // 回放上下文
                BusiContextHolder.setContext(busiContext);
                doConsume(busiContext);
            } catch (Exception e) {
                log.error("[MQ-Consumer]- 消费者消费异常！ body:{}", jsonStr, e);
            } finally {
                // 清理上下文
                BusiContextHolder.releaseContext();
            }
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
