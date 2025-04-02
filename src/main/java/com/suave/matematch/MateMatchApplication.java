package com.suave.matematch;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 启动类
 *
 */
@SpringBootApplication
@MapperScan("com.suave.matematch.mapper")
public class MateMatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MateMatchApplication.class, args);
    }

}
