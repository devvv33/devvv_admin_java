# DevvvAdmin Java

<p align="center">
  <strong>🚀 一个功能完善、可扩展的 Java 企业级基础框架</strong>
</p>

<p align="center">
  <strong>灵活切换单体/微服务架构 | 双用户体系 | 完整的企业级解决方案</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/JDK-21-blue" alt="JDK 21">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.9-green" alt="Spring Boot 3.5.9">
  <img src="https://img.shields.io/badge/Spring%20Cloud-2025.0.1-blue" alt="Spring Cloud 2025.0.1">
  <img src="https://img.shields.io/badge/License-Apache%202.0-blue" alt="License Apache 2.0">
</p>

---

## ✨ 项目简介

DevvvAdmin Java 是一个基于 **Spring Boot 3.5.9** 构建的企业级基础框架，支持**单体应用**和**微服务**两种运行模式，可根据项目规模灵活切换，无需修改业务代码。

- 🎯 **双模式运行**：单体项目直接启动，微服务模式对接 Nacos
- 👥 **双用户体系**：CMS 后台管理员 + APP 端用户，完全隔离
- 🔐 **完善鉴权**：基于 Sa-Token 的登录认证与权限控制
- 🗄️ **多数据源**：支持多数据库动态切换，统一事务管理，支持事务提交后执行业务逻辑
- 📦 **表缓存**：基于 Redis 的表级缓存，自动管理读写与失效，支持事务一致性
- ⚡ **本地缓存**：JVM 内存级缓存，Redis Pub/Sub 多实例同步
- 🔒 **分布式锁**：支持跨服务可重入的分布式锁方案
- 🔑 **接口加密**：RSA + AES 双重加密，保障数据传输安全

---

## 🛠️ 技术栈

| 技术 | 版本 | 说明                   |
|------|------|----------------------|
| JDK | 21 | 长期 LTS 版本，支持虚拟线程     |
| Spring Boot | 3.5.9 | 3.x 最新 LTS 版         |
| Spring Cloud | 2025.0.1 | 完美适配 Spring Boot 3.5.9 |
| Spring Cloud Alibaba | 2025.0.0 | 完美适配 Spring Boot 3.5.9 |
| Sa-Token | 1.44.0 | 轻量级登录鉴权框架            |
| MyBatis | 3.x | 持久层框架                |
| Druid | - | 数据库连接池               |
| Redis | 6.0+ | 缓存中间件                |
| Redisson | - | 分布式锁实现               |

---

## 📦 项目结构

```
devvv_admin_java/
├── gateway/                    # 微服务网关（微服务模式使用）
├── module-cms/                 # CMS 后台管理模块
│   ├── cms-api/               # CMS 对外接口定义
│   └── cms-biz/               # CMS 业务实现
├── module-user/               # APP 用户模块
│   ├── user-api/              # User 对外接口定义
│   └── user-biz/              # User 业务实现
├── module-commons/            # 公共基础模块
│   ├── commons-common/        # 通用工具类、常量
│   ├── commons-core/          # 核心组件（数据源、缓存、Redis等）
│   ├── commons-manager-sys/   # 系统级管理器,各业务模块共用
│   ├── commons-support-web/   # Web 支持组件
│   ├── commons-support-sa-token/  # Sa-Token 集成
│   ├── commons-support-feign/ # Feign 远程调用
│   ├── commons-support-mq/    # 消息队列支持
│   └── commons-support-xxl-job/   # XXL-JOB 定时任务
└── docs/                      # 详细文档
```

---

## 🚀 快速开始

### 环境要求

- **JDK 21+**
- **MySQL 8.0+**
- **Redis 6.0+**
- **Nacos 3.0.3+**（仅微服务模式需要）

### 1. 初始化数据库

执行 `/docs/data/mysql/` 目录下的 SQL 脚本初始化数据库。

### 2. 修改配置

根据运行模式修改 `application.yml`：

```yaml
spring:
  profiles:
    # 单体模式（推荐新手）
    active: local,dev
    
    # 微服务模式
    # active: nacos,dev
```

### 3. 启动项目

**单体模式**：直接运行 `CmsWebApplication.java` 即可

**微服务模式**：
1. 启动 Nacos 服务
2. 导入 `/docs/data/nacos/` 目录下的配置
3. 依次启动 Gateway、CMS、User 等服务

### 4. 访问系统

| 服务 | 地址 |
|------|------|
| 后台系统 | http://localhost:8801/ |


### 5. 前端项目也已开源

📢 **配套前端项目**：[DevvvAdmin](https://github.com/devvv33/devvv_admin) · [GitHub](https://github.com/devvv33/devvv_admin) | [Gitee](https://gitee.com/devvv33/devvv_admin)

---

## 🔄 运行模式

### 单体模式

适用于中小型项目，简单快速，无需依赖 Nacos。

```yaml
spring:
  profiles:
    active: local,dev
```

**特点**：
- 配置存储在本地 `application-local.yml`
- 直接启动，无需依赖Nacos
- 适合快速开发和小团队

### 微服务模式

适用于大型项目，支持服务拆分和独立部署。

```yaml
spring:
  profiles:
    active: nacos,dev
```

**特点**：
- 配置托管在 Nacos 配置中心
- 支持服务注册发现
- 支持配置动态刷新
- 适合大规模分布式部署

---

## 👥 用户体系

系统设计了两套完全独立的用户体系：

| 用户类型 | 适用场景 | 登录工具类 | 获取当前用户                                  | Token 标识 |
|----------|----------|--------|-----------------------------------------|------------|
| **Admin** | CMS 后台管理用户 | `StpAdminUtil` | `BusiContextUtil.getAdminSessionCopy()` | `cms_token` |
| **User** | APP 端普通用户 | `StpUserUtil` | `BusiContextUtil.getUserSessionCopy()`  | `sid` |

### 路由权限控制

| 路由前缀 | 权限要求 |
|----------|----------|
| `/cmsApi/**` | 必须 Admin 用户登录 |
| `/api/**` | 必须 User 用户登录 |
| `/inner/**` | 内部服务调用（需 SameToken） |

---

## 📖 接口文档

基于 **OpenAPI 3** 规范，内置 Swagger UI 接口文档。

| 模式 | 访问地址                                                       |
|------|------------------------------------------------------------|
| 微服务模式 | `http://localhost:8888/swagger-ui/index.html` (Gateway 聚合) |
| 单体模式 | `http://localhost:8801/swagger-ui/index.html`              |

---

## ⭐ 项目亮点

### 🗄️ 多数据源支持

支持多数据库动态切换，统一事务管理，支持事务提交后执行任务。

```java
@Table(DB = DBType.cms)  // 指定数据源
public interface CmsMenuMapper {
    // ...
}

@Transactional
public void updateData() {
    // 操作多个数据源，统一事务管理
    cmsMapper.update(...);
    sysMapper.update(...);
    
    // 事务提交后执行
    BusiTransactionUtil.execAfterCommit(() -> {
        LocalCache.notifyReload(...);
    });
}
```

📚 详细文档：[MySQL多数据源配置](docs/MySQL多数据源配置.md)

---

### 📦 多 Redis 数据源

支持按业务隔离 Redis 数据库，统一 Key 管理机制。

```java
@Autowired
private SysRedisTemplate sysRedisTemplate;   // 系统缓存
@Autowired
private BizRedisTemplate bizRedisTemplate;   // 业务缓存
@Autowired
private SessionRedisTemplate sessionRedisTemplate; // Session 缓存
```

📚 详细文档：[Redis使用说明](docs/Redis使用说明.md)

---

### 💾 表缓存

基于 Redis 的数据库表级缓存，自动管理缓存的读取、写入和失效，支持事务一致性。

```java
@Table(
    DB = DBType.sys,
    useTableCache = true,      // 启用表缓存
    cacheExpire = 24 * 3600    // 缓存 24 小时
)
public interface AdminUserMapper {
    AdminUser selectByPrimaryKey(Long id);  // 自动走缓存
}
```

📚 详细文档：[表缓存说明](docs/表缓存说明.md)

---

### 🚀 本地缓存

JVM 内存级缓存，通过 Redis Pub/Sub 实现多实例同步。

```java
// 获取配置
String value = SettingManager.getInstance().getString("CONFIG_KEY");

// 数据变更后通知刷新
LocalCache.notifyReload(LocalCacheEnums.SettingManager, "CONFIG_KEY");
```

📚 详细文档：[本地缓存说明](docs/本地缓存说明.md)

---

### 🔒 分布式锁

支持微服务场景的分布式锁方案，**支持跨服务可重入**。

```java
@Transactional
public void recharge(Long userId, BigDecimal amount) {
    // 锁定用户，事务结束自动释放
    BusiRedissonLockUtil.lockUserId(userId);
    
    // 安全地执行业务逻辑
    accountService.add(userId, amount);
}
```

📚 详细文档：[分布式锁使用说明](docs/分布式锁使用说明.md)

---

### 🔐 API 接口加密

RSA + AES 双重加密方案，保障接口数据传输安全。

- **x-arg**：RSA 加密的 AES 密钥
- **x-inf**：AES 加密的客户端信息
- **请求体/响应体**：AES 加密

📚 详细文档：[API接口加密流程](docs/api接口加密流程.md)

---

### 📋 BusiContext 上下文

业务上下文对象，贯穿整个请求生命周期，支持跨服务传递。

```java
// 获取当前用户
Long userId = BusiContextUtil.getUserId();
Long adminId = BusiContextUtil.getAdminId();

// 获取请求信息
String traceId = BusiContextUtil.getContext().getTraceId();
String clientIp = BusiContextUtil.getContext().getClientIp();
```

📚 详细文档：[BusiContext说明](docs/BusiContext说明.md)

---

## 📚 详细文档

| 文档 | 说明 |
|------|------|
| [登录鉴权流程](docs/登录鉴权流程.md) | Sa-Token 集成、双用户体系、权限控制 |
| [MySQL多数据源配置](docs/MySQL多数据源配置.md) | 多数据源、事务管理、分表支持 |
| [Redis使用说明](docs/Redis使用说明.md) | 多 Redis 数据源、Key 管理 |
| [表缓存说明](docs/表缓存说明.md) | 基于 Redis 的表级缓存方案 |
| [本地缓存说明](docs/本地缓存说明.md) | JVM 内存缓存、多实例同步 |
| [分布式锁使用说明](docs/分布式锁使用说明.md) | Redisson 分布式锁、跨服务重入 |
| [API接口加密流程](docs/api接口加密流程.md) | RSA + AES 双重加密方案 |
| [BusiContext说明](docs/BusiContext说明.md) | 业务上下文、链路追踪 |

---

## 📄 License

本项目采用 [Apache License 2.0](LICENSE) 开源协议。

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

如果这个项目对你有帮助，请给一个 ⭐ Star 支持一下！