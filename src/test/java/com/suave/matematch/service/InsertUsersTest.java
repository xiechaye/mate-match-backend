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
import java.util.concurrent.atomic.AtomicInteger;

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

    @Test
    void doDeleteUsers() {
        // 创建更多线程提高并行度
        int threadCount = 50; // 增加线程数
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        // 分批处理参数
        int startId = 10000;
        int endId = 1200000;
        int batchSize = 10000; // 固定批次大小更好控制
        int totalBatches = (int) Math.ceil((double)(endId - startId) / batchSize);

        // 创建任务集合
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);

        // 分配任务
        for (int i = 0; i < totalBatches; i++) {
            int batchStartId = startId + i * batchSize;
            int batchEndId = Math.min(batchStartId + batchSize, endId);

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    for (int id = batchStartId; id < batchEndId; id++) {
                        userService.removeById(id);
                        successCount.incrementAndGet();
                    }
                    System.out.println("线程" + Thread.currentThread().getName() +
                            "完成删除：" + batchStartId + "到" + batchEndId);
                } catch (Exception e) {
                    System.err.println("删除ID " + batchStartId + "-" + batchEndId + " 时出错: " + e.getMessage());
                }
            }, executorService);

            futures.add(future);
        }

        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 关闭线程池
        executorService.shutdown();
        System.out.println("所有用户删除完成，共删除 " + successCount.get() + " 条记录");
    }
}
