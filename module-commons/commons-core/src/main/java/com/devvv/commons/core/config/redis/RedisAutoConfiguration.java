package com.devvv.commons.core.config.redis;

import cn.hutool.core.util.StrUtil;
import com.devvv.commons.common.enums.redis.RedisType;
import com.devvv.commons.core.config.redis.condition.*;
import com.devvv.commons.core.config.redis.redisson.BizRedisson;
import com.devvv.commons.core.config.redis.redisson.LogRedisson;
import com.devvv.commons.core.config.redis.redisson.SysRedisson;
import com.devvv.commons.core.config.redis.redisson.UserRedisson;
import com.devvv.commons.core.config.redis.template.*;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.function.Function;

/**
 * Create by WangSJ on 2023/07/06
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisAutoConfiguration {

    @Autowired
    private RedisProperties redisProperties;

    // region  SysRedis

    /**
     * Jedis 连接池
     */
    @Lazy
    @Bean("sysJedisConnectionFactory")
    @Conditional(SysRedisCondition.class)
    public JedisConnectionFactory sysJedisConnectionFactory() {
        return buildConnectionFactory(RedisType.sys);
    }

    /**
     * SpringBoot基础的 redisTemplate
     * 同时被用作SpringBoot默认的 redisTemplate
     */
    @Lazy
    @Bean(name = {"sysSpringBootRedisTemplate", "redisTemplate"})
    @Conditional(SysRedisCondition.class)
    public RedisTemplate<String, String> sysSpringBootRedisTemplate(@Qualifier("sysJedisConnectionFactory") JedisConnectionFactory jedisConnectionFactory) {
        return generateRedisTemplate(jedisConnectionFactory);
    }

    @Lazy
    @Bean(name = "sysRedisTemplate")
    @Conditional(SysRedisCondition.class)
    public SysRedisTemplate sysRedisTemplate(@Qualifier("sysSpringBootRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        return new SysRedisTemplate(redisTemplate);
    }

    /**
     * Redisson实例
     */
    @Lazy
    @Bean
    @Conditional(SysRedisCondition.class)
    public SysRedisson sysRedisson() {
        return generateRedissonClient(RedisType.sys, SysRedisson::new);
    }
    // endregion


    // region  TableRedis
    @Lazy
    @Bean("tableJedisConnectionFactory")
    @Conditional(TableRedisCondition.class)
    public JedisConnectionFactory tableJedisConnectionFactory() {
        return buildConnectionFactory(RedisType.table);
    }

    @Lazy
    @Bean("tableSpringBootRedisTemplate")
    @Conditional(TableRedisCondition.class)
    public RedisTemplate<String, String> tableSpringBootRedisTemplate(@Qualifier("tableJedisConnectionFactory") JedisConnectionFactory jedisConnectionFactory) {
        return generateRedisTemplate(jedisConnectionFactory);
    }

    @Lazy
    @Bean(name = "tableRedisTemplate")
    @Conditional(TableRedisCondition.class)
    public TableRedisTemplate tableRedisTemplate(@Qualifier("tableSpringBootRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        return new TableRedisTemplate(redisTemplate);
    }
    // endregion


    // region  Session
    @Lazy
    @Bean("sessionJedisConnectionFactory")
    @Conditional(SessionRedisCondition.class)
    public JedisConnectionFactory sessionJedisConnectionFactory() {
        return buildConnectionFactory(RedisType.session);
    }

    @Lazy
    @Bean("sessionSpringBootRedisTemplate")
    @Conditional(SessionRedisCondition.class)
    public RedisTemplate<String, String> sessionSpringBootRedisTemplate(@Qualifier("sessionJedisConnectionFactory") JedisConnectionFactory jedisConnectionFactory) {
        return generateRedisTemplate(jedisConnectionFactory);
    }

    @Lazy
    @Bean(name = "sessionRedisTemplate")
    @Conditional(TableRedisCondition.class)
    public SessionRedisTemplate sessionRedisTemplate(@Qualifier("sessionSpringBootRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        return new SessionRedisTemplate(redisTemplate);
    }
    // endregion


    // region  UserRedis
    @Lazy
    @Bean("userJedisConnectionFactory")
    @Conditional(UserRedisCondition.class)
    public JedisConnectionFactory userJedisConnectionFactory() {
        return buildConnectionFactory(RedisType.user);
    }

    @Lazy
    @Bean("userSpringBootRedisTemplate")
    @Conditional(UserRedisCondition.class)
    public RedisTemplate<String, String> userSpringBootRedisTemplate(@Qualifier("userJedisConnectionFactory") JedisConnectionFactory jedisConnectionFactory) {
        return generateRedisTemplate(jedisConnectionFactory);
    }

    @Lazy
    @Bean(name = "userRedisTemplate")
    @Conditional(UserRedisCondition.class)
    public UserRedisTemplate userRedisTemplate(@Qualifier("userSpringBootRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        return new UserRedisTemplate(redisTemplate);
    }

    @Lazy
    @Bean
    @Conditional(UserRedisCondition.class)
    public UserRedisson userRedisson() {
        return generateRedissonClient(RedisType.user, UserRedisson::new);
    }
    // endregion


    // region  BusiRedis
    @Lazy
    @Bean("bizJedisConnectionFactory")
    @Conditional(BizRedisCondition.class)
    public JedisConnectionFactory bizJedisConnectionFactory() {
        return buildConnectionFactory(RedisType.biz);
    }

    @Lazy
    @Bean("bizSpringBootRedisTemplate")
    @Conditional(BizRedisCondition.class)
    public RedisTemplate<String, String> bizSpringBootRedisTemplate(@Qualifier("bizJedisConnectionFactory") JedisConnectionFactory jedisConnectionFactory) {
        return generateRedisTemplate(jedisConnectionFactory);
    }

    @Lazy
    @Bean(name = "bizRedisTemplate")
    @Conditional(BizRedisCondition.class)
    public BizRedisTemplate bizRedisTemplate(@Qualifier("bizSpringBootRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        return new BizRedisTemplate(redisTemplate);
    }

    @Lazy
    @Bean
    @Conditional(BizRedisCondition.class)
    public BizRedisson busiRedisson() {
        return generateRedissonClient(RedisType.biz, BizRedisson::new);
    }
    // endregion

    // region  LimterRedis
    @Lazy
    @Bean("limitJedisConnectionFactory")
    @Conditional(LimitRedisCondition.class)
    public JedisConnectionFactory limitJedisConnectionFactory() {
        return buildConnectionFactory(RedisType.limit);
    }

    @Lazy
    @Bean("limitSpringBootRedisTemplate")
    @Conditional(LimitRedisCondition.class)
    public RedisTemplate<String, String> limterSpringBootRedisTemplate(@Qualifier("limitJedisConnectionFactory") JedisConnectionFactory jedisConnectionFactory) {
        return generateRedisTemplate(jedisConnectionFactory);
    }

    @Lazy
    @Bean(name = "limitRedisTemplate")
    @Conditional(LimitRedisCondition.class)
    public LimitRedisTemplate limterRedisTemplate(@Qualifier("limitSpringBootRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        return new LimitRedisTemplate(redisTemplate);
    }
    // endregion

    // region  LogRedis
    @Lazy
    @Bean("logJedisConnectionFactory")
    @Conditional(LogRedisCondition.class)
    public JedisConnectionFactory logJedisConnectionFactory() {
        return buildConnectionFactory(RedisType.log);
    }

    @Lazy
    @Bean("logSpringBootRedisTemplate")
    @Conditional(LogRedisCondition.class)
    public RedisTemplate<String, String> logSpringBootRedisTemplate(@Qualifier("logJedisConnectionFactory") JedisConnectionFactory jedisConnectionFactory) {
        return generateRedisTemplate(jedisConnectionFactory);
    }

    @Lazy
    @Bean(name = "logRedisTemplate")
    @Conditional(LogRedisCondition.class)
    public LogRedisTemplate logRedisTemplate(@Qualifier("logSpringBootRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        return new LogRedisTemplate(redisTemplate);
    }

    @Lazy
    @Bean
    @Conditional(LogRedisCondition.class)
    public LogRedisson logRedisson() {
        return generateRedissonClient(RedisType.log, LogRedisson::new);
    }
    // endregion


    /**
     * Redisson 实例构建
     * 文档： https://github.com/redisson/redisson/wiki
     */
    public <T extends Redisson> T generateRedissonClient(RedisType redisType, Function<Config, T> function) {
        RedisConfig.RedisPoolConfig poolConfig = RedisConfig.getPoolConfig(redisType, redisProperties);

        // 构建单节点的Redisson实例
        Config redissonConfig = new Config();
        redissonConfig.setCodec(StringCodec.INSTANCE);
        SingleServerConfig singleServerConfig = redissonConfig.useSingleServer();
        singleServerConfig.setAddress(StrUtil.format("redis://{}:{}", poolConfig.getHost(), poolConfig.getPort()))
                .setDatabase(poolConfig.getDatabase())
                .setClientName(redisType.getId() + "Redisson")
                .setIdleConnectionTimeout((int) poolConfig.getMaxWaitMillis())
                .setTimeout((int) poolConfig.getTimeout());
        if (StrUtil.isNotBlank(poolConfig.getPassword())) {
            singleServerConfig.setPassword(poolConfig.getPassword());
        }
        return function.apply(redissonConfig);
    }

    /**
     * 构建 RedisTemplate
     */
    private RedisTemplate<String, String> generateRedisTemplate(JedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        // 如果不配置Serializer，那么存储的时候缺省使用String，如果用User类型存储，那么会提示错误User can't cast to String！
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setStringSerializer(new StringRedisSerializer());
        // 开启事务
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }

    /**
     * 构建 Reids连接池
     */
    private JedisConnectionFactory buildConnectionFactory(RedisType redisType) {
        RedisConfig.RedisPoolConfig poolConfig = RedisConfig.getPoolConfig(redisType, redisProperties);

        // 基础连接信息设置
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(poolConfig.getHost());
        redisStandaloneConfiguration.setPort(poolConfig.getPort());
        redisStandaloneConfiguration.setDatabase(poolConfig.getDatabase());
        // 设置密码
        if (StrUtil.isNotBlank(poolConfig.getPassword())) {
            redisStandaloneConfiguration.setPassword(RedisPassword.of(poolConfig.getPassword()));
        }
        log.warn("初始化Redis: {} {}:{}/{}", StrUtil.fixLength(redisType.getId(), ' ', 7), poolConfig.getHost(), poolConfig.getPort(), poolConfig.getDatabase());

        // 连接池配置
        JedisClientConfiguration jedisClientConfiguration = JedisClientConfiguration.builder()
                .connectTimeout(Duration.ofMillis(poolConfig.getTimeout()))
                .clientName(redisType.getId() + "Client")
                .usePooling().poolConfig(poolConfig)
                .build();
        return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
    }

}
