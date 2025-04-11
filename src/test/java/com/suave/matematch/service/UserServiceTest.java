package com.suave.matematch.service;

import com.suave.matematch.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
class UserServiceTest {
    @Resource
    private UserService userService;

    /**
     * 测试用户注册
     */
    @Test
    void testSearchUsersByTags() {
        List<String> tagNameList = List.of("Java", "Python");
        List<User> userList = userService.searchUsersByTags(tagNameList);
        Assertions.assertNotNull(userList);
    }
}