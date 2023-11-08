package org.casbin.spring.boot.autoconfigure;

import io.lettuce.core.RedisURI;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.persist.Watcher;
import org.casbin.spring.boot.autoconfigure.properties.CasbinProperties;
import org.casbin.watcherEx.RedisWatcherEx;
import org.casbin.watcherEx.WatcherOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: CasbinRedisWatcherAutoConfiguration
 * @package org.casbin.spring.boot.autoconfigure
 * @description:
 * @date 2019-4-05 13:53
 */

@Configuration
@EnableConfigurationProperties({CasbinProperties.class, RedisProperties.class})
@AutoConfigureAfter({RedisAutoConfiguration.class, CasbinAutoConfiguration.class})
@ConditionalOnExpression("'jdbc'.equalsIgnoreCase('${casbin.store-type:jdbc}') && ${casbin.enable-watcher-ex:false} && 'redis'.equalsIgnoreCase('${casbin.watcher-type:redis}') ")
public class CasbinRedisWatcherExAutoConfiguration {

    private final static Logger logger = LoggerFactory.getLogger(CasbinRedisWatcherExAutoConfiguration.class);

    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    @ConditionalOnMissingBean
    public Watcher redisWatcher(RedisProperties redisProperties, CasbinProperties casbinProperties, Enforcer enforcer) {
//        int timeout = redisProperties.getTimeout() != null ? (int) redisProperties.getTimeout().toMillis() : 2000;

        WatcherOptions options = new WatcherOptions();
        options.setChannel("jcasbin-channel");
//        options.setLocalID();
        // 选择设置，也挺重要的。决定了要不要忽略请求节点的增量更新。一般都不更新请求节点
        options.setIgnoreSelf(true);
        options.setOptions(RedisURI.builder()
                .withHost(redisProperties.getHost())
                .withPort(redisProperties.getPort())
                .withPassword(redisProperties.getPassword().toCharArray())
                .build()
        );
        RedisWatcherEx watcher = new RedisWatcherEx(options);
        enforcer.setWatcher(watcher);
        logger.info("Casbin set watcher-ex: {}", watcher.getClass().getName());
        return watcher;
    }
}