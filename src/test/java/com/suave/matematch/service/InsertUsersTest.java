package com.suave.matematch.service;

import com.suave.matematch.model.domain.User;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
@Slf4j
public class InsertUsersTest {
    @Resource
    private UserService userService;

    @Test
    void doInsertUsers() {
        StopWatch stopwatch = new StopWatch();
        final int MAX_NUM = 10000;
        List<User> userList =  new ArrayList<>();
        stopwatch.start();
        for (int i = 0; i < MAX_NUM; i++) {
            User user = new User();
            user.setUsername("suave666");
            user.setUserAccount("suave666");
            user.setAvatarUrl("https://web-tlias-suave.oss-cn-hangzhou.aliyuncs.com/0fed2382-edd5-45bd-a7b5-ecafa62e1cef.jpg");
            user.setGender(1);
            user.setUserPassword("b7d67f1450bbf3892f6aae08ed81656f");
            user.setPhone("16666666666");
            user.setEmail("dsfdsfds@gamil.com");
            user.setProfile("我很牛");
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList, 1000);
        stopwatch.stop();
        log.error("插入{}条数据耗时：{}ms", MAX_NUM, stopwatch.getTotalTimeMillis());
    }

    private ExecutorService executorService = new ThreadPoolExecutor(16,100, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));
    /**
     * 并发
     */
    @Test
    void doConcurrencyInsertUsers() {
        StopWatch stopwatch = new StopWatch();
        stopwatch.start();
        final int MAX_NUM = 100000;
        int j = 0;

        List<CompletableFuture> futureList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<User> userList = new ArrayList<>();
            while (true){
                j++;
                User user = new User();
                user.setUsername("suave666");
                user.setUserAccount("suave666");
                user.setAvatarUrl("https://web-tlias-suave.oss-cn-hangzhou.aliyuncs.com/0fed2382-edd5-45bd-a7b5-ecafa62e1cef.jpg");
                user.setGender(1);
                user.setUserPassword("b7d67f1450bbf3892f6aae08ed81656f");
                user.setPhone("16666666666");
                user.setEmail("dsfdsfds@gamil.com");
                user.setProfile("我很牛");
                user.setTags("[]");
                userList.add(user);
                if (j % 10000 == 0) {
                    break;
                }
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                log.info("线程{}开始插入数据", Thread.currentThread().getName());
                userService.saveBatch(userList, 10000);
            }, executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopwatch.stop();
        log.error("插入{}条数据耗时：{}ms", MAX_NUM, stopwatch.getTotalTimeMillis());
    }
}
