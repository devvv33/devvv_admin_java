# MySQL 多数据源配置

## 概述

本项目实现了一套基于 MyBatis + Druid 的多数据源解决方案，支持以下特性：

-  **动态数据源切换** - 基于注解自动路由到对应数据库
-  **多数据源事务** - 统一管理多个数据源的事务提交/回滚
-  **Druid 连接池** - 使用阿里巴巴 Druid 连接池，支持监控
-  **分表支持** - 基于策略的动态分表功能

## 核心组件
[datasource](../module-commons/commons-core/src/main/java/com/devvv/commons/core/config/datasource) 目录结构
```
datasource/
├── annotation/
│   └── Table.java                      # @Table 注解，指定数据源
├── routing/
│   ├── RoutingContext.java             # 路由上下文
│   ├── RoutingContextHolder.java       # 路由上下文持有者（ThreadLocal）
│   └── RoutingTableAspect.java         # AOP 切面，自动切换数据源
├── transaction/
│   ├── MultiDataSourceTransaction.java         # 多数据源事务
│   ├── MultiDataSourceTransactionFactory.java  # 事务工厂
│   ├── MultiDatasourceTransactionManager.java  # 多数据源事务管理器
│   ├── MultiDataSourceSynchronizationManager.java  # 数据源同步管理
│   ├── busi/
│   │   ├── BusiTransactionManager.java         # 业务事务管理器
│   │   ├── BusiTransactionResource.java        # 事务资源接口
│   │   └── BusiTransactionResourceManager.java # 事务资源管理
│   └── ...
├── sharding/
│   ├── ITableShardStrategy.java        # 分表策略接口
│   ├── ShardByPackage.java             # 按包分表策略示例
│   └── TableShardPlugin.java           # MyBatis 分表插件
├── DataSourceProperties.java           # 数据源配置属性
├── DataSourceConfig.java               # 数据源配置类
├── DruidConfig.java                    # Druid 监控配置
└── MultiDataSource.java                # 多数据源实现
```

---

## 配置说明

### 1. YAML 配置示例

```yaml
############################### MySQL 配置 ###############################
dataSource:
  # 驱动类名
  driverClassName: com.mysql.cj.jdbc.Driver
  
  # 公共配置（所有数据源共享，可被单独配置覆盖）
  initSize: 1                       # 初始连接数
  maxActive: 50                     # 最大连接数(并发数)
  minIdle: 3                        # 最小连接池数量
  connectionTimeout: 60000          # 获取连接时最大等待时间，单位毫秒
  validationQuery: SELECT 1         # 验证连接的SQL
  testWhileIdle: true               # 在空闲时检查有效性
  testOnBorrow: false               # 申请连接时检测连接是否有效
  testOnReturn: false               # 归还连接时检测连接是否有效
  timeBetweenEvictionRunsMillis: 60000   # 间隔多久进行检测，关闭空闲连接
  idleTimeout: 600000               # 连接空闲的最长时间（毫秒）
  
  # 公共账号密码（可被单独配置覆盖）
  username: admin
  password: 123456
  
  # 各数据源的单独配置
  types:
    sys:    # 系统配置库
      jdbcUrl: jdbc:mysql://127.0.0.1:3306/dvvv_sys?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    cms:    # CMS 数据库
      jdbcUrl: jdbc:mysql://127.0.0.1:3306/devvv_cms?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
    query:  # 分析库（可使用不同的账号密码）
      jdbcUrl: jdbc:mysql://192.168.1.100:3306/analytics?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
      username: query_user
      password: query_password
      maxActive: 100  # 覆盖公共配置
  
  # 启用的数据源（逗号分隔）
  enable-types: cms,sys
```

### 2. 数据源类型枚举

在 `DBType` 枚举中定义支持的数据源类型：

```java
public enum DBType implements IDEnum {
    sys("sys", "配置数据库"),
    user("user", "用户数据库"),
    busi("busi", "业务数据库"),
    cms("cms", "CMS数据库"),
    query("query", "分析库"),
    ;
    
    private final String id;
    private final String desc;
}
```

---

## 使用方式

### 1. 在Mapper接口上添加 `@Table` 注解 ，指定使用的数据源：

```java
@Table(DB = DBType.cms)  // 指定使用 cms 数据库
public interface CmsMenuMapper {
    
    int deleteByPrimaryKey(Long id);

    int insert(CmsMenu record);

    CmsMenu selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CmsMenu record);
}
```

### 2. @Table 注解属性

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

    /**
     * 表名，大小写不敏感
     */
    String value() default "";

    /**
     * 数据库类型
     */
    DBType DB() default DBType.sys;

    /**
     * 分表策略 Bean 名称
     */
    String shardBy() default "";

    /**
     * 是否启用表缓存
     */
    boolean useTableCache() default false;
    
    /**
     * 是否缓存空值
     */
    boolean cacheNullValue() default false;
    
    /**
     * 缓存过期时间（秒）
     */
    int cacheExpire() default 24 * 3600;
    
    /**
     * 主键字段名
     */
    String[] primaryKey() default {"id"};
}
```

---

## 事务管理

### 1. 基本用法

使用 Spring 标准的 `@Transactional` 注解，事务由 `BusiTransactionManager` 统一管理：

```java
@Service
public class SyncService {
    
    @Autowired
    private CmsMenuMapper cmsMenuMapper;  // cms 数据源
    
    @Autowired
    private SysConfigMapper sysConfigMapper;  // sys 数据源
    
    @Transactional  // 统一事务管理
    public void syncData() {
        // 操作 cms 数据库
        cmsMenuMapper.insert(menu);
        
        // 操作 sys 数据库
        sysConfigMapper.insert(config);
        
        // 如果发生异常，两个数据源都会回滚
    }
}
```

### 2. 事务提交后执行任务

#### 使用场景

在业务开发中，经常需要在**数据库事务提交成功后**再执行某些操作，例如：

- 刷新本地缓存（确保读到最新数据）
- 发送消息通知
- 调用外部接口
- 记录操作日志

如果在事务提交前执行这些操作，可能会出现以下问题：

| 问题 | 说明 |
|------|------|
| 缓存不一致 | 事务回滚后，缓存已经被更新，导致脏数据 |
| 消息丢失 | 事务回滚后，消息已发送但数据未入库 |
| 幻读 | 缓存刷新时读到的是事务未提交的旧数据 |

#### BusiTransactionUtil.execAfterCommit()

使用 `BusiTransactionUtil.execAfterCommit()` 可以确保任务在事务**成功提交后**才执行：

```java
@Transactional
public void createAdmin(AdminUserForm form) {
    // 1. 数据库操作
    cmsAdminUserMapper.insertSelective(admin);
    userRoleMapper.insertBatch(roleList);
    
    // 2. 注册事务提交后的回调任务
    BusiTransactionUtil.execAfterCommit(() -> {
        // 这里的代码会在事务成功提交后执行
        LocalCache.notifyReload(LocalCacheEnums.AdminUserManager, admin.getAdminId().toString());
    });
}
```

#### 工作原理

```
┌─────────────────────────────────────────────────────────────────┐
│                      @Transactional 方法                         │
├─────────────────────────────────────────────────────────────────┤
│  1. 执行数据库操作                                                │
│     └── INSERT/UPDATE/DELETE                                     │
│                                                                  │
│  2. 调用 execAfterCommit() 注册回调任务                           │
│     └── 任务被缓存到 ThreadLocal 队列中（不立即执行）               │
│                                                                  │
│  3. 方法正常结束                                                  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
              ┌───────────────┴───────────────┐
              ↓                               ↓
      ┌──────────────┐               ┌──────────────┐
      │  事务提交成功  │               │  事务回滚     │
      └──────────────┘               └──────────────┘
              ↓                               ↓
      ┌──────────────┐               ┌──────────────┐
      │ 执行所有回调  │               │ 清空任务队列  │
      │ 刷新缓存...   │               │ 任务不执行    │
      └──────────────┘               └──────────────┘
```

#### 智能执行机制

`execAfterCommit()` 会根据当前是否在事务环境中，自动选择执行策略：

| 场景 | 行为 |
|------|------|
| 在 `@Transactional` 方法内 | 任务延迟到事务提交后执行 |
| 不在事务环境中 | 任务立即执行 |
| 事务回滚 | 任务被丢弃，不执行 |

#### 实际使用示例

**示例1：更新用户信息后刷新缓存**

```java
@Transactional
public void updateAdmin(AdminUserForm form) {
    // 数据库更新
    cmsAdminUserMapper.updateByPrimaryKeySelective(update);
    userRoleMapper.deleteByAdminId(update.getAdminId());
    userRoleMapper.insertBatch(newRoleList);
    
    // 事务后执行
    BusiTransactionUtil.execAfterCommit(() -> {
        // 刷新缓存（必须等事务提交后）
        LocalCache.notifyReload(LocalCacheEnums.AdminUserManager, update.getAdminId().toString());
        // 踢出登录
        StpAdminUtil.kickout(update.getAdminId());
    });
}
```

### 3. 自定义事务资源

如果需要更复杂的事务控制，可以实现 `BusiTransactionResource` 接口：

```java
public interface BusiTransactionResource {
    
    /** 事务开始时调用 */
    void begin() throws Throwable;
    
    /** 事务提交后调用 */
    void commit() throws Throwable;
    
    /** 事务回滚后调用 */
    void rollback() throws Throwable;
    
    /** 执行顺序（数值越小越先执行） */
    default int order() { return 0; }
}
```

**使用场景：**
- 分布式锁的释放
- 外部系统的补偿操作
- 自定义的两阶段提交逻辑

---

## 工作原理

### 数据源路由流程

```
1. 调用 Mapper 方法
       ↓
2. RoutingTableAspect 拦截（AOP）
       ↓
3. 读取 @Table 注解，获取 DBType
       ↓
4. 设置 RoutingContextHolder（ThreadLocal）
       ↓
5. MultiDataSource.determineCurrentLookupKey() 
       ↓
6. 返回对应的 DataSource
       ↓
7. 执行 SQL
       ↓
8. 清理 RoutingContextHolder
```

### 多数据源事务流程

```
1. @Transactional 开启事务
       ↓
2. BusiTransactionManager.getTransaction()
       ↓
3. 初始化 MultiDataSourceSynchronizationManager
   初始化 BusiTransactionResourceManager
       ↓
4. 执行业务逻辑
   - 操作多个数据源（连接自动注册）
   - 调用 execAfterCommit() 注册回调任务
       ↓
5. 事务提交/回滚
       ↓
6. 统一提交或回滚所有数据源连接
       ↓
7. 触发 BusiTransactionResource 的 commit/rollback
   （包括 execAfterCommit 注册的任务）
       ↓
8. 清理所有资源
```



---

## 分表功能

### 1. 实现分表策略

```java
@Component("ShardByMonth")
public class ShardByMonth implements ITableShardStrategy {
    
    @Override
    public String getNewTableName(String tableName) {
        String suffix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return tableName + "_" + suffix;
    }
}
```

### 2. 使用分表

```java
@Table(DB = DBType.busi, value = "order", shardBy = "ShardByMonth")
public interface OrderMapper {
    // order 表会自动替换为 order_202501 等
    List<Order> listByUserId(@Param("userId") Long userId);
}
```

---

## Druid 监控

### 访问监控页面

在 `dev` 或 `test` 环境下，可以访问 Druid 监控控制台：

```
http://localhost:8080/druid/
```

### 配置认证（可选）

在 `DruidConfig` 中配置登录账号密码：

```java
@Bean
@Profile({"dev", "test"})
public ServletRegistrationBean monitor() {
    ServletRegistrationBean<StatViewServlet> bean = 
        new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");
    
    HashMap<String, String> initParameters = new HashMap<>();
    initParameters.put("loginUsername", "admin");
    initParameters.put("loginPassword", "123456");
    bean.setInitParameters(initParameters);
    
    return bean;
}
```

---

## 最佳实践

### 1. 按业务划分数据源

```yaml
types:
  sys:    # 系统配置、权限等
  user:   # 用户相关数据
  busi:   # 核心业务数据
  query:  # 只读分析库
```

### 2. 事务建议

- 尽量让事务在单个数据源内完成
- 跨数据源事务要注意性能影响
- 只读操作建议使用 `@Transactional(readOnly = true)`

### 4. 连接池调优

```yaml
# 生产环境建议配置
initSize: 5
maxActive: 100
minIdle: 10
connectionTimeout: 30000
testWhileIdle: true
testOnBorrow: false
timeBetweenEvictionRunsMillis: 60000
idleTimeout: 300000
```


---

## 相关文档
- [Druid 配置文档](https://github.com/alibaba/druid/wiki/DruidDataSource%E9%85%8D%E7%BD%AE)
