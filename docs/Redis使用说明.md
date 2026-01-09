# Redis 多数据源配置

本框架提供了一套完整的 Redis 多数据源解决方案，支持按业务场景隔离不同的 Redis 数据库，并提供了统一的 Key 管理机制和丰富的操作封装。

## 目录结构
[redis](../module-commons/commons-core/src/main/java/com/devvv/commons/core/config/redis)目录结构:
```
redis/
├── RedisAutoConfiguration.java   # 自动配置类，核心入口
├── RedisProperties.java          # 配置属性映射
├── RedisConfig.java              # 配置工具类
├── RedisKey.java                 # Key 构建器
├── condition/                    # 条件注解类
│   ├── SysRedisCondition.java
│   ├── BizRedisCondition.java
│   ├── LimitRedisCondition.java
│   ├── LogRedisCondition.java
│   ├── SessionRedisCondition.java
│   ├── TableRedisCondition.java
│   └── UserRedisCondition.java
├── template/                     # Redis操作模板类
│   ├── AbstractRedisTemplate.java  # 抽象基类，封装所有操作
│   ├── SysRedisTemplate.java
│   ├── BizRedisTemplate.java
│   ├── LimitRedisTemplate.java
│   ├── LogRedisTemplate.java
│   ├── SessionRedisTemplate.java
│   ├── TableRedisTemplate.java
│   └── UserRedisTemplate.java
├── key/                          # Key定义
│   ├── KeyDefine.java            # Key定义接口
│   ├── SysKeyDefine.java
│   ├── BusiKeyDefine.java
│   ├── LimitKeyDefine.java
│   ├── LogKeyDefine.java
│   ├── UserKeyDefine.java
│   └── RedisKeyCheckAspect.java  # Key使用规范检查切面
├── redisson/                     # Redisson客户端
│   ├── SysRedisson.java
│   ├── BizRedisson.java
│   ├── LogRedisson.java
│   └── UserRedisson.java
└── mode/                         # 模式枚举
    ├── KeyMode.java              # Key模式
    ├── TTLMode.java              # 过期时间模式
    └── DataMode.java             # 数据类型模式
```

---

## 支持的数据源类型

| 类型      | 说明         | RedisTemplate       | Redisson     |
|---------|------------|---------------------|--------------|
| `sys`     | 系统缓存       | SysRedisTemplate    | SysRedisson  |
| `limit`   | 阈值限定（限流等）  | LimitRedisTemplate  | -            |
| `session` | Session 缓存 | SessionRedisTemplate| -            |
| `table`   | 表缓存        | TableRedisTemplate  | -            |
| `biz`     | 通用业务缓存     | BizRedisTemplate    | BizRedisson  |
| `user`    | 用户缓存       | UserRedisTemplate   | UserRedisson |
| `log`     | 日志缓存       | LogRedisTemplate    | LogRedisson  |

---

## 配置说明

### YAML 配置示例
    配置属性优先级: 单独配置 > 公共配置 > 默认值
```yaml
redis:
  # ========== 公共配置（所有数据源共享，可被单独配置覆盖）==========
  host: 127.0.0.1           # Redis 服务地址
  port: 6379                # Redis 端口
  password:                 # Redis 密码（可选）

  # 连接池配置
  maxIdle: 10               # 最大空闲连接数
  maxTotal: 30              # 最大连接数
  minIdle: 5                # 最小空闲连接数
  timeout: 20000            # 连接超时时间（毫秒）
  maxWaitMillis: 10000      # 获取连接最大等待时间（毫秒）
  
  # 连接检测
  testOnBorrow: true        # 从连接池获取连接时是否检测
  testOnCreate: false       # 创建连接时是否检测
  testOnReturn: false       # 归还连接时是否检测
  testWhileIdle: false      # 空闲时是否检测

  # ========== 各数据源的单独配置（可覆盖公共配置）==========
  types:
    sys:
      database: 0           # 使用的数据库索引
    biz:
      database: 1
    limit:
      database: 2
    session:
      database: 3
    table:
      database: 4
    user:
      database: 5
      # 可以单独配置不同的 Redis 服务器
      # host: 192.168.1.100
      # port: 6380
      # password: xxx
    log:
      database: 6

  # ========== 启用的数据源（逗号分隔）==========
  enable-types: sys,biz,limit,session,table,user,log
```


---

## 使用指南

### 统一的 Key 管理

每个数据源对应一个 KeyDefine 枚举类，用于统一管理该数据源的所有 Key：
-- SysKeyDefine.java
-- LimitKeyDefine.java
-- UserKeyDefine.java

### 注入不同的 RedisTemplate 即使用对应的redis数据源

```java
@Service
public class MyService {

    // 注入不同的 RedisTemplate
    @Autowired
    private SysRedisTemplate sysRedisTemplate;
    @Autowired
    private BizRedisTemplate bizRedisTemplate;

    public void example() {
        // 先要在KeyDefine中定义key
        RedisKey key = RedisKey.create(SysKeyDefine.TEST, "param1");
        
        // 设置值（自动设置过期时间）
        sysRedisTemplate.set(key, "value");
    }
}
```

## 架构特性

### 1. 懒加载

所有 Bean 都使用 `@Lazy` 注解，只有在实际使用时才会初始化连接池，节省启动时间和资源。

### 2. 条件装配

通过 `@Conditional` 注解和 `enable-types` 配置，只创建需要的数据源，按需加载。

### 3. 配置继承

单独配置可以覆盖公共配置，避免重复配置，同时支持不同数据源连接不同的 Redis 实例。

### 4. 统一序列化

所有 RedisTemplate 都使用 `StringRedisSerializer`，避免乱码问题。

### 5. 自动过期管理

通过 `TTLMode` 机制，支持多种过期时间策略，减少手动设置过期时间的工作。

---

## 常见问题

### Q1: 为什么要区分不同的数据源？

1. **隔离性**：不同业务使用不同的 database，避免 Key 冲突
2. **可维护性**：每个数据源有独立的 KeyDefine，Key 管理更清晰
3. **灵活性**：可以为不同业务配置不同的 Redis 实例
4. **监控性**：方便针对不同业务进行监控和调优

### Q2: TTLMode.DYNAMIC 什么时候使用？

当同一个 Key 在不同场景下需要不同的过期时间时使用：

```java
// 根据用户等级设置不同的过期时间
int ttl = user.isVip() ? 7200 : 3600;
RedisKey key = RedisKey.create(SysKeyDefine.USER_CACHE, userId).setTTL(ttl);
```
