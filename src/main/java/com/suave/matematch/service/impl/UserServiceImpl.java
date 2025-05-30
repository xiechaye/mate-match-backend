package com.suave.matematch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.suave.matematch.common.ErrorCode;
import com.suave.matematch.common.ResultUtils;
import com.suave.matematch.contant.RedisConstant;
import com.suave.matematch.contant.UserConstant;
import com.suave.matematch.exception.BusinessException;
import com.suave.matematch.model.domain.User;
import com.suave.matematch.service.UserService;
import com.suave.matematch.mapper.UserMapper;
import com.suave.matematch.utils.AlgorithmUtils;
import com.suave.matematch.utils.OssUtils;
import com.suave.matematch.utils.RedisUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.suave.matematch.contant.UserConstant.ADMIN_ROLE;
import static com.suave.matematch.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author Suave
 */
@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "Suave";

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        // 昵称默认为用户名
        user.setUsername(userAccount);
        // 默认头像
        user.setAvatarUrl(UserConstant.DEFAULT_AVATAR_URL);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.USER_NOT_EXIST, "请检查用户名和密码");
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户(内存查询)
     *
     * @param tagNameList 标签名列表
     * @return 用户列表
     */
    public List<User> searchUsersByTags (List<String> tagNameList) {
        // 内存查询
        if(tagNameList == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<User> qw = new QueryWrapper<>();
        // 查询所有用户
        List<User> userList = userMapper.selectList(qw);

        return userList.stream().filter(user -> {
            if(user.getTags() == null) {
                return false;
            }

            // 解析JSON字符串
            Gson gson = new Gson();
            Set<String> tagNameSet = gson.fromJson(user.getTags(), new TypeToken<Set<String>>() {});
            // 判断tagNameSet是否为空
            tagNameSet = Optional.ofNullable(tagNameSet).orElse(new HashSet<>());
            // 标签匹配
            for (String tagName : tagNameList) {
                if(!tagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).toList();
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     *
     * @param user 用户信息
     * @return
     */
    public boolean isAdmin(User user) {
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    public User getLoginUser(HttpServletRequest request) {
        if(request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = (User) request.getSession().getAttribute(USER_LOGIN_STATE);

        return user;
    }

    /**
     * 更新用户信息
     * @param user 用户信息
     * @param request 登录用户
     * @return
     */
    public int updateUser(User user, HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        long userId = user.getId();
        if(userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询用户
        User oldUser = userMapper.selectById(userId);
        if(oldUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }

        if(!isAdmin(user) && !Objects.equals(user.getId(), loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        // 更改信息
        int i = userMapper.updateById(user);
        if(i == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新失败");
        }
        // 更新成功后，删除缓存
        this.userLogout(request);
        return i;
    }

    /**
     * 获取推荐默认用户列表
     *
     * @param pageSize 单页数据数
     * @param pageNum 页码
     * @param request
     * @return
     */
    public List<User> recommendUsers(Long pageSize, Long pageNum, HttpServletRequest request) {
        String redisKey = RedisConstant.RECOMMEND_KEY;
        // 获取用户id
        User user = getLoginUser(request);
        if(user == null || user.getId() <= 0) {
            // 定义缓存key
            redisKey = String.format("%s:all", redisKey);
        }else {
            redisKey = String.format("%s:%s", redisKey, user.getId());
        }

        // 查询缓存
        List<User> userList = RedisUtils.get(redisKey);
        if(userList != null) {
            return userList;
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(user != null) {
            queryWrapper.ne("id", user.getId());
        }
        IPage<User> userIPage = userMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);

        // 脱敏
        userList = userIPage.getRecords().stream().map(this::getSafetyUser).toList();

        // 向缓存插入数据
        RedisUtils.set(redisKey, userList);
        return userList;
    }

    /**
     * 匹配用户（推荐用户）
     * @param num 用户个数
     * @param loginUser 登录用户
     * @return
     */
    public List<User> matchUser(long num, User loginUser) {
        // 定义推荐用户的缓存key
        String matchUserRedisKey = String.format("%s:%s", RedisConstant.MATE_MATCH_KEY, loginUser.getId());
        // 查询所有用户的redisKey
        String allUserRedisKey = String.format("%s:allUser", RedisConstant.MATE_MATCH_KEY);

        // 查询缓存
        List<User> redisUserList = RedisUtils.get(matchUserRedisKey);
        if(redisUserList != null) {
            return redisUserList;
        }
        String tags = loginUser.getTags();
        // 如果用户没有标签，则随机推荐（默认前num个用户）
        if(StringUtils.isBlank(tags)) {
            Page<User> page = this.page(new Page<>(0, num));
            if(page.getTotal() == 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            return page.getRecords().stream().map(this::getSafetyUser).toList();
        }
        Gson gson = new Gson();
        List<String> userTageList = gson.fromJson(tags, new TypeToken<List<String>>() {}.getType());

        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.isNotNull("tags");
        // 只需要查询id和tags（提升性能）
        qw.select("id", "tags");


        // 查询缓存
        List<User> userList = RedisUtils.get(allUserRedisKey);
        if(userList == null) {
            userList = this.list(qw);
            RedisUtils.set(allUserRedisKey, userList);
        }

        // 存储匹配用户
        List<Pair<User, Integer>> list = new ArrayList<>();
        // 遍历用户列表
        for (User user : userList) {
            String matchUserTags = user.getTags();
            // 如果用户没有标签或查询出的用户是登录用户，则跳过
            if(StringUtils.isBlank(matchUserTags) || Objects.equals(user.getId(), loginUser.getId())) {
                continue;
            }

            // 解析JSON字符串
            List<String> matchUserTagList = gson.fromJson(matchUserTags, new TypeToken<List<String>>() {}.getType());

            // 计算匹配度
            int distance = AlgorithmUtils.minDistance(userTageList, matchUserTagList);
            Pair<User, Integer> pair = Pair.of(user, distance);
            list.add(pair);
        }

        // 获取匹配度前num个用户id(按照匹配度升序排列)
        List<Long> matchUserIdList = list.stream()
                .sorted((a, b) -> a.getSecond() - b.getSecond())
                .map(pair -> pair.getFirst().getId())
                .limit(num)
                .toList();

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", matchUserIdList);
        // 查询用户信息
        List<User> matchUserList = this.list(queryWrapper);
        // 将用户信息与用户id进行映射
        Map<Long, User> matchUserMap = matchUserList.stream().collect(Collectors.toMap(User::getId, this::getSafetyUser));

        // 存储最终匹配的用户(脱敏，排序)
        List<User> finalMatchUserList = new ArrayList<>();
        for (Long id : matchUserIdList) {
            finalMatchUserList.add(matchUserMap.get(id));
        }

        // 向缓存插入数据
        RedisUtils.set(matchUserRedisKey, finalMatchUserList);
        return finalMatchUserList;
    }

    /**
     * 上传头像
     * @param multipartFile 头像文件
     * @param loginUser 登录用户
     * @return
     */
    public String uploadAvatar(MultipartFile multipartFile, User loginUser) {
        // 2. 文件校验（大小、类型等） - 可选但推荐
        // 例如：限制文件大小不超过2MB
        if (multipartFile.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "头像文件过大，请上传小于2MB的文件");
        }
        // 例如：限制文件类型
        String contentType = multipartFile.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/gif"))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的头像文件格式，请上传JPG, PNG, GIF格式");
        }

        byte[] fileBytes;
        try {
            // 4. 从 MultipartFile 获取字节数组
            fileBytes = multipartFile.getBytes();
        } catch (IOException e) {
            log.error("获取上传文件字节流失败. UserID: {}", loginUser.getId(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取上传文件内容失败: " + e.getMessage());
        }

        // 5. 准备 objectName (OSS中的对象键)
        String originalFilename = multipartFile.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String ossDirectory = "mate-match/avatars/" + loginUser.getId() + "/"; // Ensure trailing slash
        String fileNameInOss = UUID.randomUUID().toString() + fileExtension;
        String objectName = ossDirectory + fileNameInOss;

        String avatarUrl = OssUtils.uploadImage(fileBytes, objectName);

        // 4. 更新用户数据库中的头像URL
        User user = new User();
        user.setId(loginUser.getId());
        user.setAvatarUrl(avatarUrl);
        int i = userMapper.updateById(user);
        if (i <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像更新失败");
        }
        return avatarUrl;
    }

    /**
     * 根据标签搜索用户(sql查询)
     *
     * @param tagNameList 标签名列表
     * @return 用户列表
     */
    @Deprecated
    private List<User> searchUsersByTagsBySQL (List<String> tagNameList) {
        // SQL查询
        if(tagNameList == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 模糊匹配
        QueryWrapper<User> qw = new QueryWrapper<>();
        for (String tag : tagNameList) {
            qw.like("tags", tag);
        }
        List<User> users = userMapper.selectList(qw);

        // 脱敏
        return users.stream().map(this::getSafetyUser).toList();
    }
}
