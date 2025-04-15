package com.suave.matematch.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置类
 *
 * @author Suave
 */
@Configuration
@ConfigurationProperties(prefix = "spring.data.redisson")
@Data
public class RedissonConfig {
    private String port;
    private String password;
    private String host;
    private Integer database;


    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = String.format("redis://%s:%s", host, port);
        config.useSingleServer()
                .setAddress(address)
                .setPassword(password)
                .setDatabase(database);

        return Redisson.create(config);
    }
}
