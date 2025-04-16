package com.suave.matematch.service;

import com.suave.matematch.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import static com.suave.matematch.contant.UserConstant.ADMIN_ROLE;
import static com.suave.matematch.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务
 *
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser 原始用户信息
     * @return 脱敏后的用户信息
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     * @return 注销状态
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     * @param tagNameList 标签名列表
     * @return 用户列表
     */
    List<User> searchUsersByTags (List<String> tagNameList);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param user 用户
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 获取登录用户
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 更新用户信息
     * @param user 用户信息
     * @param loginUser 登录用户
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取推荐用户列表
     * @param pageSize 单页数据数
     * @param pageNum 页码
     * @param request
     * @return
     */
    List<User> recommendUsers(Long pageSize, Long pageNum, HttpServletRequest request);
}
