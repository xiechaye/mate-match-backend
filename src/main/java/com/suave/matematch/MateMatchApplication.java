package com.suave.matematch;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类
 *
 */
@SpringBootApplication
@MapperScan("com.suave.matematch.mapper")
@EnableScheduling
public class MateMatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MateMatchApplication.class, args);
    }

}
