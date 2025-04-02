package com.suave.matematch.service;

import com.suave.matematch.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class UserServiceTest {
    @Resource
    private UserService userService;

    @Test
    void testSearchUsersByTags() {
        List<String> tagNameList = List.of("Java", "Python");
        List<User> userList = userService.searchUsersByTags(tagNameList);
        Assertions.assertNotNull(userList);
    }
}