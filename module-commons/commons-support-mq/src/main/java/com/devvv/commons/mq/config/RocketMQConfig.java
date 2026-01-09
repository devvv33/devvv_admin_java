package com.devvv.commons.mq.config;

import com.devvv.commons.core.config.ApplicationInfo;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.remoting.RPCHook;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Create by WangSJ on 2024/07/01
 */
@Configuration
public class RocketMQConfig {

    @Resource
    private RocketMQProperties rocketMQProperties;

    private DefaultMQProducer defaultMQProducer;

    /**
     * 设置阿里云账号的AccessKey ID和AccessKey Secret。
     */
    public RPCHook getAclRPCHook() {
        return new AclClientRPCHook(new SessionCredentials(rocketMQProperties.getAccessKey(), rocketMQProperties.getAccessSecret()));
    }

    public String getTag(String originTag) {
        if ("*".equals(originTag)) {
            return originTag;
        }
        return originTag + "_" + rocketMQProperties.getEnv();
    }

    public String getGroupId(String originGroupId) {
        return originGroupId + "_" + rocketMQProperties.getEnv();
    }

    /**
     * 生产者
     * 文档： https://help.aliyun.com/zh/apsaramq-for-rocketmq/cloud-message-queue-rocketmq-4-x-series/developer-reference/three-modes-used-to-send-normal-messages?spm=a2c4g.11186623.0.i8#section-lo2-s3c-i94
     */
    @Bean
    public DefaultMQProducer defaultMQProducer() throws MQClientException {
        /**
         *创建Producer，并开启消息轨迹。设置为您在阿里云消息队列RocketMQ版控制台创建的Group ID。
         *如果不想开启消息轨迹，可以按照如下方式创建：
         *DefaultMQProducer producer = new DefaultMQProducer("YOUR GROUP ID", getAclRPCHook());
         */
        String producerGroup = getGroupId(ApplicationInfo.CURRENT_APP_TYPE.name());
        defaultMQProducer = new DefaultMQProducer(producerGroup, getAclRPCHook(), true, null);
        /**
         *设置使用接入方式为阿里云，在使用云上消息轨迹的时候，需要设置此项，如果不开启消息轨迹功能，则运行不设置此项。
         */
        defaultMQProducer.setAccessChannel(AccessChannel.CLOUD);
        /**
         *设置为您从阿里云消息队列RocketMQ版控制台获取的接入点信息，类似“http://MQ_INST_XXXX.aliyuncs.com:80”。
         */
        defaultMQProducer.setNamesrvAddr(rocketMQProperties.getNameServerAddr());
        defaultMQProducer.start();
        return defaultMQProducer;
    }

    @PreDestroy
    public void onShutdown() {
        // 在应用程序关闭时执行的代码
        if (defaultMQProducer != null) {
            defaultMQProducer.shutdown();
        }
    }
}
