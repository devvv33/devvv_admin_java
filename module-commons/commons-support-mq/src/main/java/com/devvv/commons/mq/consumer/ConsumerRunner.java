package com.devvv.commons.mq.consumer;

import cn.hutool.core.util.StrUtil;
import com.devvv.commons.common.exception.BusiException;
import com.devvv.commons.common.response.ErrorCode;
import com.devvv.commons.mq.config.RocketMQConfig;
import com.devvv.commons.mq.config.RocketMQProperties;
import com.devvv.commons.common.enums.mq.MQTags;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Create by WangSJ on 2024/07/01
 */
@Slf4j
@Component
public class ConsumerRunner implements ApplicationRunner {

    @Resource
    private RocketMQProperties rocketMQProperties;
    @Resource
    private RocketMQConfig rocketMQConfig;

    @Autowired(required = false)
    private List<ConsumerListenerExecutor> list;
    private List<DefaultMQPushConsumer> consumerList = new ArrayList<>();

    /**
     * 项目启动后，注册消费者逻辑
     *
     * 阿里云ons对接文档： https://help.aliyun.com/zh/apsaramq-for-rocketmq/cloud-message-queue-rocketmq-4-x-series/developer-reference/three-modes-used-to-send-normal-messages?spm=a2c4g.11186623.0.i8#section-lo2-s3c-i94
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (list == null || list.isEmpty()) {
            return;
        }

        for (ConsumerListenerExecutor consumerListener : list) {
            // 1、 获取注解
            ConsumerListener listener = consumerListener.getClass().getAnnotation(ConsumerListener.class);
            if (listener == null) {
                log.error("[MQ-Consumer]- 消费者未配置注解@RocketMQListener class:{}", consumerListener.getClass());
                throw new BusiException(ErrorCode.ERR);
            }

            String topicId = listener.topic().getId();
            MQTags[] tags = listener.tags();
            String subExpression = tags == null || tags.length == 0 ? "*" :
                    Arrays.stream(tags)
                            .map(MQTags::getId)
                            .map(rocketMQConfig::getTag)
                            .reduce((v1, v2) -> v1 + "||" + v2)
                            .orElse("*");
            String groupId = rocketMQConfig.getGroupId(listener.groupId());
            int threadNum = listener.threadNum();

            /**
             * 创建Consumer
             */
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(groupId, rocketMQConfig.getAclRPCHook(), new AllocateMessageQueueAveragely(), true, null);
            //设置为阿里云消息队列RocketMQ版实例的接入点。
            consumer.setNamesrvAddr(rocketMQProperties.getNameServerAddr());
            //阿里云上消息轨迹需要设置为CLOUD方式，在使用云上消息轨迹的时候，需要设置此项，如果不开启消息轨迹功能，则运行不设置此项。
            consumer.setAccessChannel(AccessChannel.CLOUD);
            consumer.setConsumeThreadMin(threadNum);
            consumer.setConsumeThreadMax(threadNum);
            consumer.subscribe(topicId, subExpression);
            consumer.registerMessageListener(consumerListener);
            consumer.start();
            consumerList.add(consumer);
            log.info("[MQ-Consumer]- 启动监听 Topic:{} Tags:{} Group:{} threadNum：{}", StrUtil.padAfter(topicId, 7, " "), StrUtil.padAfter(subExpression, 15, " "),StrUtil.padAfter(groupId, 15, " "), threadNum);
        }
    }


    @PreDestroy
    public void onShutdown() {
        for (DefaultMQPushConsumer consumer : consumerList) {
            try {
                if (consumer != null) {
                    consumer.shutdown();
                }
            } catch (Exception e) {
                log.error("[MQ-Consumer]- 消费者关闭异常！", e);
            }
        }
    }

}
