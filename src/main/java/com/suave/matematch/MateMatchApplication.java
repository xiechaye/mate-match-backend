package com.suave.matematch;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 启动类
 *
 */
@SpringBootApplication
@MapperScan("com.suave.matematch.mapper")
@EnableScheduling
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 60 * 60, redisNamespace = "mate:match:session")
public class MateMatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MateMatchApplication.class, args);
    }

}
