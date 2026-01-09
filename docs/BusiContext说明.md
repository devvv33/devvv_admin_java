# BusiContext 业务上下文说明

## 一、概述

`BusiContext` 是本系统的**业务上下文对象**，用于在单次请求/任务的整个生命周期中传递和共享数据。它解决了以下核心问题：

| 问题 | 解决方案 |
|------|---------|
| traceId 链路追踪 | 在入口处生成，贯穿整个调用链 |
| 当前登录用户传递 | 存储用户ID和会话信息副本 |
| 客户端信息传递 | 解析并存储客户端设备、版本等信息 |
| 自定义业务数据传递 | 通过 ext 扩展字段存储 |

---

## 二、核心类

| 文件路径                                                            | 说明                          |
|-----------------------------------------------------------------|-----------------------------|
| `commons-core/.../context/BusiContext.java`                     | 上下文数据对象                     |
| `commons-core/.../context/BusiContextHolder.java`               | ThreadLocal 持有者             |
| `commons-core/.../context/BusiContextUtil.java`                 | 便捷工具类                       |
| `commons-core/.../context/ClientInfo.java`                      | 客户端信息对象                     |
| `commons-core/.../context/ClientInfoUtil.java`                  | 客户端信息解析/校验工具                |
| `commons-support-web/.../filter/RootFilter.java`                | 根过滤器（创建/清理Context）          |
| `commons-support-web/.../filter/ApiFilter.java`                 | API过滤器（解析ClientInfo）        |
| `commons-support-web/.../filter/InnerFilter.java`               | 内部调用过滤器（恢复Context）          |
| `commons-support-xxl-job/.../aop/XxlJobAspect.java`             | XXL-JOB切面（创建/清理Context）     |
| `commons-core/.../config/AsyncTaskConfig.java`                  | Spring Task配置（创建/清理Context） |
| `commons-core/.../utils/BusiThreadPoolUtil.java`                | 自定义线程池                      |
| `commons-support-mq/.../consumer/ConsumerListenerExecutor.java` | MQ消费者（恢复/清理Context）         |

---

## 三、生命周期管理

### 3.1 创建时机

BusiContext 在不同入口处创建：

| 入口类型            | 创建位置 | 说明                  |
|-----------------|---------|---------------------|
| HTTP请求          | `RootFilter.initBusiContext()` | 所有HTTP请求的入口         |
| XXL-JOB定时任务     | `XxlJobAspect.doAspect()` | XXL-JOB任务执行时        |
| Spring Task定时任务 | `AsyncTaskConfig.beforeExecute()` | @Scheduled任务执行时     |
| 线程池            | `BusiThreadPoolUtil.wrapped()` | 使用线程池时,将当前上下文传递给子线程 |
| MQ消费            | `ConsumerListenerExecutor.consumeMessage()` | 从MQ消息中恢复Context     |

### 3.2 销毁时机

**必须在流程结束时调用 `BusiContextHolder.releaseContext()` 释放上下文**，防止 ThreadLocal 内存泄漏。

| 入口类型 | 销毁位置 |
|---------|---------|
| HTTP请求 | `RootFilter.doFilter()` 的 finally 块 |
| XXL-JOB定时任务 | `XxlJobAspect.doAspect()` 的 finally 块 |
| Spring Task定时任务 | `AsyncTaskConfig.afterExecute()` |
| 线程池 | `BusiThreadPoolUtil.wrapped()` 的 finally 块|
| MQ消费 | `ConsumerListenerExecutor.consumeMessage()` 的 finally 块 |

---

## 四、请求处理流程

### 4.1 HTTP 请求处理链

```
请求进入
    ↓
┌─────────────────────────────────────────────────────────────────┐
│ RootFilter (/* 所有请求)                                          │
│ 1. 创建 BusiContext                                               │
│ 2. 设置 traceId、requestTime、clientIp、requestURL、requestURI 等  │
│ 3. 存入 ThreadLocal                                               │
└─────────────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────────────┐
│ ApiFilter (/api/*, /cmsApi/*)  或  InnerFilter (/inner/*)        │
│ - ApiFilter: 解析 x-inf/x-arg 请求头，设置 ClientInfo              │
│ - InnerFilter: 从 __context__ 请求头恢复上游服务的 Context         │
└─────────────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────────────┐
│ SaInterceptor (Sa-Token 拦截器)                                   │
│ - 登录校验、权限校验、客户端校验                                     │
└─────────────────────────────────────────────────────────────────┘
    ↓
┌─────────────────────────────────────────────────────────────────┐
│ ParseTokenUserInterceptor                                        │
│ - 从 Token 解析用户信息                                            │
│ - 将 adminId/userId 和 Session 信息写入 BusiContext               │
└─────────────────────────────────────────────────────────────────┘
    ↓
Controller 处理业务
    ↓
┌─────────────────────────────────────────────────────────────────┐
│ RootFilter finally                                               │
│ 1. 打印请求日志                                                    │
│ 2. 调用 BusiContextHolder.releaseContext() 清理上下文              │
└─────────────────────────────────────────────────────────────────┘
```

### 4.2 Filter 职责说明

| Filter | URL Pattern | 职责 |
|--------|-------------|------|
| `RootFilter` | `/*` | 创建Context、异常处理、日志打印、清理Context |
| `ApiFilter` | `/api/*`, `/cmsApi/*` | 解析客户端信息 (ClientInfo) |
| `InnerFilter` | `/inner/*` | 恢复上游服务传递的Context |
| `CorsFilter` | `/*` | 处理跨域请求 |

---

## 五、上下文传递

### 5.1 Feign 远程调用传递

通过 `FeignInterceptor` 在请求头中传递序列化后的 Context：

```java
// FeignInterceptor.apply()
String contextJson = BusiContextHolder.getContext().toJsonStr();
String contextBase64 = Base64.encode(contextJson);
requestTemplate.header(BusiContextHolder.CONTEXT_KEY, contextBase64);
```

下游服务通过 `InnerFilter` 恢复：

```java
// InnerFilter.processInnerContext()
String contextBase64 = request.getHeader(BusiContextHolder.CONTEXT_KEY);
BusiContext remoteContext = JSONObject.parseObject(Base64.decodeStr(contextBase64), BusiContext.class);
// 合并 traceId：上游traceId:本服务traceId
remoteContext.setTraceId(remoteContext.getTraceId() + ":" + context.getTraceId());
```

### 5.2 MQ 消息传递

发送消息时将 Context 作为消息体发送：

```java
// 发送时
String contextJson = BusiContextHolder.getContext().toJsonStr();
// 作为消息体发送
```

消费时恢复 Context：

```java
// ConsumerListenerExecutor.consumeMessage()
String jsonStr = new String(msg.getBody());
BusiContext busiContext = JSONObject.parseObject(jsonStr, BusiContext.class);
BusiContextHolder.setContext(busiContext);
```

### 5.3 线程池传递

使用自定义的线程池 `BusiThreadPoolUtil.executeDefaultPool(..)`，在任务执行前后处理 Context：

```java
public static <T> Future<T> executeDefaultPool(Callable<T> callable) {
    return DEFAULT_VIRTUAL_THREAD.submit(wrapped(callable));
}

private static Runnable wrapped(Runnable runnable) {
    BusiContext ctx = BusiContextHolder.getContext();
    return () -> {
        // 传递上下文
        if (ctx != null) {
            BusiContextHolder.setContext(ctx);
            MDC.put("traceId", ctx.getTraceId());
        }
        // 执行任务并清理
        try {
            runnable.run();
        } finally {
            BusiContextHolder.releaseContext();
        }
    };
}
```

---

## 六、使用示例

### 6.1 获取当前登录用户

```java
// 推荐方式：使用 BusiContextUtil
Long userId = BusiContextUtil.getUserId();
Long adminId = BusiContextUtil.getAdminId();
String adminName = BusiContextUtil.getAdminName();
AdminSessionInfo adminSession = BusiContextUtil.getAdminSessionCopy();

// 判断是否为超级管理员
if (BusiContextUtil.isSuperAdmin()) {
    // ...
}
```

### 6.2 获取请求信息

```java
BusiContext context = BusiContextUtil.getContext();
String traceId = context.getTraceId();
String clientIp = context.getClientIp();
String requestURI = context.getRequestURI();
```

### 6.3 获取客户端信息

```java
ClientInfo clientInfo = BusiContextUtil.getContext().getClientInfo();
if (clientInfo != null) {
    ClientType clientType = clientInfo.getClientType();  // IOS/Android
    String version = clientInfo.getClientVersion();
    String deviceId = clientInfo.getDeviceId();
}
```

### 6.4 使用扩展字段

```java
// 存储
BusiContextUtil.getContext().getExt().put("customKey", "customValue");

// 读取
String value = BusiContextUtil.getContext().getExt().getString("customKey");
```

### 6.5 终止MQ消息发送

```java
// 在某些业务场景下，可以阻止后续的MQ消息发送
BusiContextUtil.breakSendMQ();
```
