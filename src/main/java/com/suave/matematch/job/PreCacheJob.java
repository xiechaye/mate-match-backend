package com.suave.matematch.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.suave.matematch.mapper.UserMapper;
import com.suave.matematch.model.domain.User;
import com.suave.matematch.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;

    private List<Long> mainUserIdList = Arrays.asList(1L);

    /**
     * 定时任务，缓存推荐用户
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void doCacheRecommendUsers() {
        RLock lock = redissonClient.getLock("matematch:precahejob:docache:lock");
        try {
            if(lock.tryLock(0,30,TimeUnit.MINUTES)){
                mainUserIdList.forEach(userId -> {
                    String redisKey = "matematch:recommend";
                    redisKey = String.format("%s:%s", redisKey, userId);

                    Page<User> page = new Page<>(1, 10);
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    IPage<User> userIPage = userMapper.selectPage(page, queryWrapper);

                    // 脱敏
                    List<User> userList = userIPage.getRecords()
                            .stream()
                            .map(user -> userService.getSafetyUser(user))
                            .toList();

                    // 获取String类型的操作对象
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    // 向缓存插入数据
                    try {
                        valueOperations.set(redisKey, userList, 1, TimeUnit.DAYS);
                    } catch (Exception e) {
                        log.error("Redis set error: {}", e.getMessage());
                    }
                });
            }
        } catch (InterruptedException e) {
            log.error("Redis lock error: {}", e.getMessage());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 定时任务，预热未登录用户推荐
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void doCacheUnLoginRecommendUsers() {
        RLock lock = redissonClient.getLock("matematch:precahejob:dounlogincache:lock");
        try {
            if(lock.tryLock(0, 5, TimeUnit.MINUTES)){
                String redisKey = "matematch:recommend";
                redisKey = String.format("%s:%s", redisKey, "all");

                Page<User> page = new Page<>(1, 10);
                QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                IPage<User> userIPage = userMapper.selectPage(page, queryWrapper);

                // 脱敏
                List<User> userList = userIPage.getRecords()
                        .stream()
                        .map(user -> userService.getSafetyUser(user))
                        .toList();

                // 获取String类型的操作对象
                ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                // 向缓存插入数据
                try {
                    valueOperations.set(redisKey, userList, 1, TimeUnit.DAYS);
                } catch (Exception e) {
                    log.error("Redis set error: {}", e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            log.error("Redis lock error: {}", e.getMessage());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }
}
