package com.devvv.commons.mq.demo;

import com.devvv.commons.common.enums.mq.MQTags;
import com.devvv.commons.core.busicode.BusiCode;
import com.devvv.commons.core.busicode.BusiCodeDefine;
import com.devvv.commons.core.context.BusiContext;
import com.devvv.commons.mq.consumer.ConsumerListener;
import com.devvv.commons.mq.consumer.ConsumerListenerExecutor;
import org.springframework.stereotype.Component;

/**
 * Create by WangSJ on 2024/07/02
 */
public class MQDemo {

    // 1、用@BusiCode + sendMQ 标记要发送MQ消息
    @BusiCode(value = BusiCodeDefine.LoginByMobile, sendMQTags = MQTags.Login)
    public void loginByMobile() {
        // todo: 业务处理
    }


    // 2、 新建Consumer，监听并处理消息
    @Component
    @ConsumerListener(tags = {MQTags.Login,MQTags.User}, groupId = "Login")
    public class MyConsumer extends ConsumerListenerExecutor {
        @Override
        public void doConsume(BusiContext context) {

            doProcess(BusiCodeDefine.LoginByMobile, "", () -> {
                System.out.println("用户登录了");
            });

        }
    }

}
