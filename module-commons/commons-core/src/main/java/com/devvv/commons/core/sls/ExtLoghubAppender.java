package com.devvv.commons.core.sls;

import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Opt;
import com.aliyun.openservices.aliyun.log.producer.LogProducer;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.aliyun.log.producer.ProducerConfig;
import com.aliyun.openservices.aliyun.log.producer.ProjectConfig;
import com.aliyun.openservices.aliyun.log.producer.errors.ProducerException;
import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.logback.LoghubAppender;
import com.devvv.commons.core.context.BusiContext;
import com.devvv.commons.core.context.BusiContextHolder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Create by WangSJ on 2024/07/10
 * 拷贝自 {@link LoghubAppender}
 * 本处调整： 添加自定义字段
 */
public class ExtLoghubAppender<E> extends UnsynchronizedAppenderBase<E> {

    private static final Logger log = LoggerFactory.getLogger(ExtLoghubAppender.class);
    private String project;

    private String endpoint;

    private String accessKeyId;

    private String accessKeySecret;

    private String userAgent = "logback";

    protected Encoder<E> encoder;

    protected ProducerConfig producerConfig = new ProducerConfig();
    protected ProjectConfig projectConfig;

    protected Producer producer;

    protected String logStore; //
    protected String topic = ""; //
    protected String source = ""; //

    protected String timeZone = "UTC";
    protected String timeFormat = "yyyy-MM-dd'T'HH:mmZ";
    protected DateTimeFormatter formatter;

    protected java.time.format.DateTimeFormatter formatter1;
    private String mdcFields;

    @Override
    public void start() {
        try {
            doStart();
        } catch (Exception e) {
            addError("Failed to start LoghubAppender.", e);
        }
    }

    private void doStart() {
        try {
            formatter = DateTimeFormat.forPattern(timeFormat).withZone(DateTimeZone.forID(timeZone));
        }catch (Exception e){
            formatter1 = java.time.format.DateTimeFormatter.ofPattern(timeFormat).withZone(ZoneId.of(timeZone));
        }
        producer = createProducer();
        super.start();
    }

    public Producer createProducer() {
        projectConfig = buildProjectConfig();
        Producer producer = new LogProducer(producerConfig);
        producer.putProjectConfig(projectConfig);
        return producer;
    }

    private ProjectConfig buildProjectConfig() {
        return new ProjectConfig(project, endpoint, accessKeyId, accessKeySecret, null, userAgent);
    }

    @Override
    public void stop() {
        try {
            doStop();
        } catch (Exception e) {
            addError("Failed to stop LoghubAppender.", e);
        }
    }

    private void doStop() throws InterruptedException, ProducerException {
        if (!isStarted()) {
            return;
        }

        super.stop();
        producer.close();
    }

    @Override
    public void append(E eventObject) {
        try {
            appendEvent(eventObject);
        } catch (Exception e) {
            addError("Failed to append event.", e);
        }
    }

    private void appendEvent(E eventObject) {
        //init Event Object
        if (!(eventObject instanceof LoggingEvent)) {
            return;
        }
        LoggingEvent event = (LoggingEvent) eventObject;

        List<LogItem> logItems = new ArrayList<LogItem>();
        LogItem item = new LogItem();
        logItems.add(item);
        item.SetTime((int) (event.getTimeStamp() / 1000));

        if(formatter!=null){
            DateTime dateTime = new DateTime(event.getTimeStamp());
            item.PushBack("time", dateTime.toString(formatter));
        }else {
            Instant instant = Instant.ofEpochMilli(event.getTimeStamp());
            item.PushBack("time", formatter1.format(instant));
        }

        item.PushBack("level", event.getLevel().toString());
        item.PushBack("thread", event.getThreadName());

        // mark: 添加自定义字段
        BusiContext busiContext = BusiContextHolder.getContext();
        if (busiContext != null) {
            Opt.ofBlankAble(busiContext.getTraceId()).ifPresent(s -> item.PushBack("traceId", s));
            Opt.ofNullable(busiContext.getUserId()).ifPresent(s -> item.PushBack("userId", s.toString()));
            Opt.ofNullable(busiContext.getAdminId()).ifPresent(s -> item.PushBack("adminId", s.toString()));
            Opt.ofNullable(busiContext.getGlobalRequestId()).ifPresent(s -> item.PushBack("globalRequestId", s.toString()));
            Opt.ofBlankAble(busiContext.getRequestURI()).ifPresent(s -> item.PushBack("url", s));
        }

        StackTraceElement[] caller = event.getCallerData();
        if (caller != null && caller.length > 0) {
            item.PushBack("location", caller[0].toString());
        }

        String message = event.getFormattedMessage();
        item.PushBack("message", message);

        IThrowableProxy iThrowableProxy = event.getThrowableProxy();
        if (iThrowableProxy != null) {
            String throwable = getExceptionInfo(iThrowableProxy);
            throwable += fullDump(event.getThrowableProxy().getStackTraceElementProxyArray());
            item.PushBack("throwable", throwable);
        }

        if (this.encoder != null) {
            item.PushBack("log", new String(this.encoder.encode(eventObject)));
        }

        Optional.ofNullable(mdcFields).ifPresent(
                f->event.getMDCPropertyMap().entrySet().stream()
                        .filter(v-> Arrays.stream(f.split(",")).anyMatch(i->i.equals(v.getKey())))
                        .forEach(map-> item.PushBack(map.getKey(),map.getValue()))
        );
        try {
            producer.send(projectConfig.getProject(), logStore, topic, source, logItems, result -> {
                if (!result.isSuccessful()) {
                    Console.error("阿里云日志发送失败！ ");
                }
            });
        } catch (Exception e) {
            this.addError(
                    "Failed to send log, project=" + project
                            + ", logStore=" + logStore
                            + ", topic=" + topic
                            + ", source=" + source
                            + ", logItem=" + logItems, e);
        }
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    private String getExceptionInfo(IThrowableProxy iThrowableProxy) {
        String s = iThrowableProxy.getClassName();
        String message = iThrowableProxy.getMessage();
        return (message != null) ? (s + ": " + message) : s;
    }

    private String fullDump(StackTraceElementProxy[] stackTraceElementProxyArray) {
        StringBuilder builder = new StringBuilder();
        for (StackTraceElementProxy step : stackTraceElementProxyArray) {
            builder.append(CoreConstants.LINE_SEPARATOR);
            String string = step.toString();
            builder.append(CoreConstants.TAB).append(string);
            ThrowableProxyUtil.subjoinPackagingData(builder, step);
        }
        return builder.toString();
    }

    public String getLogStore() {
        return logStore;
    }

    public void setLogStore(String logStore) {
        this.logStore = logStore;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    // **** ==- ProjectConfig -== **********************
    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getTotalSizeInBytes() {
        return producerConfig.getTotalSizeInBytes();
    }

    public void setTotalSizeInBytes(int totalSizeInBytes) {
        producerConfig.setTotalSizeInBytes(totalSizeInBytes);
    }

    public long getMaxBlockMs() {
        return producerConfig.getMaxBlockMs();
    }

    public void setMaxBlockMs(long maxBlockMs) {
        producerConfig.setMaxBlockMs(maxBlockMs);
    }

    public int getIoThreadCount() {
        return producerConfig.getIoThreadCount();
    }

    public void setIoThreadCount(int ioThreadCount) {
        producerConfig.setIoThreadCount(ioThreadCount);
    }

    public int getBatchSizeThresholdInBytes() {
        return producerConfig.getBatchSizeThresholdInBytes();
    }

    public void setBatchSizeThresholdInBytes(int batchSizeThresholdInBytes) {
        producerConfig.setBatchSizeThresholdInBytes(batchSizeThresholdInBytes);
    }

    public int getBatchCountThreshold() {
        return producerConfig.getBatchCountThreshold();
    }

    public void setBatchCountThreshold(int batchCountThreshold) {
        producerConfig.setBatchCountThreshold(batchCountThreshold);
    }

    public int getLingerMs() {
        return producerConfig.getLingerMs();
    }

    public void setLingerMs(int lingerMs) {
        producerConfig.setLingerMs(lingerMs);
    }

    public int getRetries() {
        return producerConfig.getRetries();
    }

    public void setRetries(int retries) {
        producerConfig.setRetries(retries);
    }

    public int getMaxReservedAttempts() {
        return producerConfig.getMaxReservedAttempts();
    }

    public void setMaxReservedAttempts(int maxReservedAttempts) {
        producerConfig.setMaxReservedAttempts(maxReservedAttempts);
    }

    public long getBaseRetryBackoffMs() {
        return producerConfig.getBaseRetryBackoffMs();
    }

    public void setBaseRetryBackoffMs(long baseRetryBackoffMs) {
        producerConfig.setBaseRetryBackoffMs(baseRetryBackoffMs);
    }

    public long getMaxRetryBackoffMs() {
        return producerConfig.getMaxRetryBackoffMs();
    }

    public void setMaxRetryBackoffMs(long maxRetryBackoffMs) {
        producerConfig.setMaxRetryBackoffMs(maxRetryBackoffMs);
    }

    public Encoder<E> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<E> encoder) {
        this.encoder = encoder;
    }

    public void setMdcFields(String mdcFields) {
        this.mdcFields = mdcFields;
    }
}