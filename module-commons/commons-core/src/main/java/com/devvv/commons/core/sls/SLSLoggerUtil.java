package com.devvv.commons.core.sls;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Opt;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.aliyun.openservices.aliyun.log.producer.LogProducer;
import com.aliyun.openservices.aliyun.log.producer.ProducerConfig;
import com.aliyun.openservices.aliyun.log.producer.ProjectConfig;
import com.aliyun.openservices.log.common.LogContent;
import com.aliyun.openservices.log.common.LogItem;
import com.devvv.commons.common.response.ErrorCode;
import com.devvv.commons.common.response.IResult;
import com.devvv.commons.common.utils.MyStrUtil;
import com.devvv.commons.core.config.ApplicationInfo;
import com.devvv.commons.core.context.BusiContext;
import com.devvv.commons.core.context.BusiContextHolder;
import com.devvv.commons.core.context.BusiContextUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Create by WangSJ on 2024/07/10
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "aliyun.sls.project")
public class SLSLoggerUtil implements InitializingBean, DisposableBean {

    private static LogProducer producer;
    private static AliyunSLSProperties aliyunSLSProperties;
    @Autowired
    private void setAliyunSLSProperties(AliyunSLSProperties aliyunSLSProperties) {
        SLSLoggerUtil.aliyunSLSProperties = aliyunSLSProperties;
    }
    @Override
    public void afterPropertiesSet() {
        producer = new LogProducer(new ProducerConfig());
        ProjectConfig projectConfig = new ProjectConfig(aliyunSLSProperties.getProject(), aliyunSLSProperties.getEndpoint(), aliyunSLSProperties.getAccessKeyId(), aliyunSLSProperties.getAccessKeySecret());
        producer.putProjectConfig(projectConfig);
    }
    @Override
    public void destroy() throws Exception {
        if (producer != null) {
            producer.close();
        }
    }

    /**
     * 记录接口访问日志
     */
    public static void sendApiLog(){
        if (producer == null) {
            log.warn("SLS日志发送失败，日志发送者尚未初始化！");
            return;
        }
        BusiContext context = BusiContextHolder.getContext();
        if (context == null) {
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();
        LogItem log = new LogItem((int) (currentTimeMillis / 1000));
        Opt.ofBlankAble(context.getRequestURL()).ifPresent(s -> log.PushBack("url", s));
        Opt.ofBlankAble(context.getRequestQuery()).ifPresent(s ->log.PushBack("reqQuery", s));
        Opt.ofBlankAble(context.getRequestBody()).ifPresent(s ->log.PushBack("reqBody", s));

        Opt.ofBlankAble(context.getTraceId()).ifPresent(s -> log.PushBack("traceId", s));
        Opt.ofNullable(context.getGlobalRequestId()).ifPresent(s -> log.PushBack("globalRequestId", s.toString()));
        Opt.ofBlankAble(context.getClientIp()).ifPresent(s -> log.PushBack("clientIp", s));
        Opt.ofBlankAble(context.getUserAgent()).ifPresent(s -> log.PushBack("ua", s));
        Opt.ofNullable(context.getClientInfo()).ifPresent(s -> log.PushBack("clientInfo", JSONObject.toJSONString(s)));
        Opt.ofNullable(context.getUserId()).ifPresent(s -> log.PushBack("userId", s.toString()));
        Opt.ofNullable(context.getAdminId()).ifPresent(s -> log.PushBack("adminId", s.toString()));
        Opt.ofNullable(context.getBusiCode()).ifPresent(s ->log.PushBack("busiCode", s.name()));
        Opt.ofBlankAble(context.getResult()).ifPresent(s ->log.PushBack("result", MyStrUtil.maxLength(JSONObject.toJSONString(s), aliyunSLSProperties.getResultMaxLength())));
        Opt.ofBlankAble(context.getError()).ifPresent(s ->log.PushBack("error", s));

        Opt.ofNullable(context.getResponse()).map(HttpServletResponse::getStatus).map(String::valueOf).ifPresent(s -> log.PushBack("status", s));
        Opt.ofNullable(context.getRequestTime()).map(t -> currentTimeMillis - t.getTime()).ifPresent(s -> log.PushBack("useTime", s.toString()));
        try {
            producer.send(aliyunSLSProperties.getProject(),
                    aliyunSLSProperties.getApiLogStore(),
                    ApplicationInfo.CURRENT_APP_TYPE.getId(),
                    ApplicationInfo.SERVER_IP,
                    log);
        } catch (Exception e) {
            SLSLoggerUtil.log.error("SLS日志发送失败！", e);
        }
    }

    public static String buildApiInfoLogMsg(){
        long useTime = System.currentTimeMillis() - BusiContextUtil.getContext().getRequestTime().getTime();

        String respStr;
        Object result = BusiContextUtil.getContext().getResult();
        // 404，可能是遭遇探测或攻击，此时打印客户端信息便于排查分析
        if (result == null && Opt.ofNullable(BusiContextUtil.getContext().getResponse()).map(resp -> resp.getStatus() == 404).orElse(false)) {
            respStr = StrUtil.format("  --CTX-- {}", BusiContextUtil.getContext().toFullJsonStr());
        } else if (result instanceof IResult r) {           // 常规返回时，直接输出返回内容
            String msg = ErrorCode.SUCCESS.getCode().equals(r.getCode()) ? (r.getData() == null ? "" : JSONObject.toJSONString(r.getData())) : r.getMsg();
            respStr = StrUtil.format("{}:{}", r.getCode(), MyStrUtil.maxLength(msg, 500));
        } else {                                            // 其他情况，输出原始返回值
            respStr = JSONObject.toJSONString(result);
        }
        return StrUtil.format("{} {} {}", BusiContextUtil.getContext().getRequestURI(), DateUtil.formatBetween(useTime), respStr);
    }



    /**
     * 发送流式日志
     * 一般用于记录 低价值、大数据量的日志
     */
    public static void sendBusiLog(String topic, Map<String, Object> msg) {
        if (producer == null) {
            return;
        }
        LogContent[] array = msg.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null)
                .map(e -> new LogContent(e.getKey(), e.getValue().toString()))
                .toArray(LogContent[]::new);
        ArrayList<LogContent> contentList = new ArrayList<>(Arrays.asList(array));
        LogItem log = new LogItem((int) (System.currentTimeMillis() / 1000), contentList);
        try {
            producer.send(aliyunSLSProperties.getProject(),
                    aliyunSLSProperties.getBusiStore(),
                    topic,
                    ApplicationInfo.SERVER_IP,
                    log);
        } catch (Exception e) {
            SLSLoggerUtil.log.error("SLS日志发送失败！", e);
        }
    }

}
