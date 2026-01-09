package com.devvv.commons.mq.aop;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.IdUtil;
import com.devvv.commons.common.enums.mq.MQTags;
import com.devvv.commons.common.enums.mq.MQTopic;
import com.devvv.commons.core.busicode.BusiCode;
import com.devvv.commons.core.config.datasource.transaction.busi.BusiTransactionResourceManager;
import com.devvv.commons.core.context.BusiContext;
import com.devvv.commons.core.context.BusiContextHolder;
import com.devvv.commons.mq.config.RocketMQConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Create by WangSJ on 2024/07/01
 */
@Slf4j
@Aspect
@Component
@Order(Integer.MAX_VALUE)
public class BusiMQAspect {

    private static final String DEFAULT_SEND_MQ_RESOURCE = "DEFAULT_SEND_MQ_RESOURCE";

    @Resource
    private DefaultMQProducer defaultMQProducer;
    @Resource
    private RocketMQConfig rocketMQConfig;


    @Around("@annotation(busiCode)")
    public Object beforeMethodWithAnnotation(ProceedingJoinPoint point, BusiCode busiCode) throws Throwable {
        // 执行原方法
        Object result = point.proceed();

        // 不发送消息
        MQTopic topic = busiCode.sendMQTopic();
        MQTags[] tags = busiCode.sendMQTags();
        if (topic == null) {
            return result;
        }

        // 中断发送消息
        BusiContext context = BusiContextHolder.getContext();
        if (context == null || BooleanUtil.isTrue(context.getBreakSendMQ())) {
            return result;
        }

        // 如果没事务，直接发送
        if (!BusiTransactionResourceManager.inTransaction()) {
            send(topic.getId(), tags, context, busiCode);
            return result;
        }
        // 有事务，放到队列中，等待事务结束时执行
        if (!BusiTransactionResourceManager.hasResource(DEFAULT_SEND_MQ_RESOURCE)) {
            BusiTransactionResourceManager.bindResource(DEFAULT_SEND_MQ_RESOURCE, new SendMQTransactionResource());
        }
        SendMQTransactionResource buffer = BusiTransactionResourceManager.getResource(DEFAULT_SEND_MQ_RESOURCE);
        buffer.addTask(() -> send(topic.getId(), tags, context, busiCode));
        return result;
    }

    /**
     * 发送消息
     * 文档: https://help.aliyun.com/zh/apsaramq-for-rocketmq/cloud-message-queue-rocketmq-4-x-series/developer-reference/three-modes-used-to-send-normal-messages?spm=a2c4g.11186623.0.i8#section-lo2-s3c-i94
     */
    public void send(String topic, MQTags[] tags, BusiContext context, BusiCode busiCode) {
        String msgStr = context.toJsonStr();
        if (log.isDebugEnabled()) {
            log.debug("[MQ-Producer]- 发送消息->{}:{}: {}", topic, tags, msgStr);
        }

        byte[] body = msgStr.getBytes(StandardCharsets.UTF_8);
        String keys = IdUtil.fastSimpleUUID();
        int flag = 0;
        boolean waitStoreMsgOK = true;

        // todo: 经测试，如果1条消息包含多个tag，将无法被正常匹配，这可能是RocketMQ版本的问题，当前我们使用的时阿里云RocketMQ4.X
        //  这里我们在多tag时，分开发送
        //  此方案有隐患: 如果业务预期给消息打上2个tag TagA和TagB，这里我们实际将其拆成2条消息发了出去，但如果Consumer中订阅了TagA||TagB，将会收到2条消息 造成重复消费（可在消费时根据keys做幂等处理）
        for (MQTags tag : tags) {
            String realTag = rocketMQConfig.getTag(tag.getId());
            Message msg = new Message(topic, realTag, keys, flag, body, waitStoreMsgOK);
            try {
                // 同步发消息
                // SendResult send = defaultMQProducer.send(msg);
                // 异步发消息
                defaultMQProducer.send(msg, new SendCallback() {
                    @Override
                    public void onSuccess(SendResult result) {
                        // 消费发送成功。
                    }

                    @Override
                    public void onException(Throwable throwable) {
                        log.error("[MQ-Producer]- 发送消息失败！ msg:{}", msgStr, throwable);
                    }
                });
            } catch (Exception e) {
                log.error("[MQ-Producer]- 发送消息失败！ msg:{}", msgStr, e);
                // throw new BusiException(ErrorCode.MQ_ERROR);
            }
        }
    }

}
