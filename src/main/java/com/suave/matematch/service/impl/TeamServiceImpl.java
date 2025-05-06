package com.suave.matematch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suave.matematch.common.ErrorCode;
import com.suave.matematch.exception.BusinessException;
import com.suave.matematch.mapper.UserTeamMapper;
import com.suave.matematch.model.domain.Team;
import com.suave.matematch.model.domain.User;
import com.suave.matematch.model.domain.UserTeam;
import com.suave.matematch.model.domain.enums.TeamStatusEnum;
import com.suave.matematch.model.domain.request.TeamQuery;
import com.suave.matematch.model.domain.request.TeamUpdateRequest;
import com.suave.matematch.model.domain.vo.TeamVo;
import com.suave.matematch.service.TeamService;
import com.suave.matematch.mapper.TeamMapper;
import com.suave.matematch.service.UserService;
import com.suave.matematch.service.UserTeamService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
* @author Suave
*/
@Service
@AllArgsConstructor
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService{
    private final UserTeamService userTeamService;
    private final UserTeamMapper userTeamMapper;
    private final UserService userService;
    /**
     * 创建队伍
     * @param team 队伍信息
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Long addTeam(Team team) {
        //1. 请求参数是否为空？
        if(team == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        final Long userId = team.getUserId();
        //2. 是否登录，未登录不允许创建
        if(userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
        }
        //3. 检验消息
        //   1. 队伍人数>1且<=20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不合法");
        }
        //   2. 队伍标题长度<20
        String name = team.getName();
        if(StringUtils.isBlank(name) || name.length() >= 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题错误");
        }
        //   3. 描述<=512
        String description = team.getDescription();
        if(StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述错误");
        }
        //   4. status是否公开（int）不传默认是0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if(teamStatusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态错误");
        }
        //   5. 如果status是加密状态，一定要有密码，且密码<=32
        String password = team.getPassword();
        if (status == TeamStatusEnum.SECRET.getValue() && StringUtils.isBlank(password) || password.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置错误");
        }
        //   6. 超时时间>当前时间
        Date expireTime = team.getExpireTime();
        if(expireTime == null || expireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "超时时间错误");
        }
        //   7. 用户最多创建5个队伍
        // todo 可能会出现并发问题，例如同时创建100个队伍
        QueryWrapper<Team> qw = new QueryWrapper<>();
        qw.eq("userId", userId);
        long count = this.count(qw);
        if(count >= 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户创建队伍数量超过限制");
        }
        //4. 插入队伍信息到队伍表
        team.setExpireTime(new Date());
        boolean save = this.save(team);
        if(!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍失败");
        }

        Long teamId = team.getId();
        //5. 插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(team.getExpireTime());
        save = userTeamService.save(userTeam);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建队伍关联表失败");
        }

        return teamId;
    }

    /**
     * 查询队伍列表
     * @param teamQuery 队伍信息
     * @param isAdmin 是否是管理员
     * @return
     */
    public List<TeamVo> getTeamList(TeamQuery teamQuery, Boolean isAdmin) {
        // 分页参数
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> qw = new QueryWrapper<>();
        // 从请求参数中取出队伍名称等查询条件，如果存在则作为查询条件
        // 根据id查询
        Long id = teamQuery.getId();
        if(id != null && id > 0) {
            qw.eq("id", id);
        }
        // 根据关键字对队伍名称和描述进行模糊查询
        String searchText = teamQuery.getSearchText();
        if(StringUtils.isNotBlank(searchText)) {
            qw.and(queryWrapper -> queryWrapper.like("name", searchText)
                    .or().like("description", searchText));
        }
        // 根据队伍名称查询
        String name = teamQuery.getName();
        if(StringUtils.isNotBlank(name)) {
            qw.eq("name", name);
        }
        // 根据队伍描述查询
        String description = teamQuery.getDescription();
        if(StringUtils.isNotBlank(description)) {
            qw.eq("description", description);
        }
        // 根据最大人数查询
        Integer maxNum = teamQuery.getMaxNum();
        if(maxNum != null && maxNum > 0) {
            qw.eq("maxNum", maxNum);
        }
        // 根据过期时间查询
        Date expireTime = teamQuery.getExpireTime();
        if (expireTime != null) {
            qw.and(queryWrapper -> queryWrapper.gt("expireTime", expireTime)
                    .or().isNull("expireTime"));
        } else {
            // 当expireTime为null时，可能不需要添加过期时间条件
            // 或者只添加isNull条件
            qw.or().isNull("expireTime");
        }
        // 根据创建者id查询
        Long userId = teamQuery.getUserId();
        if(userId != null && userId > 0) {
            qw.eq("userId", userId);
        }
        // 根据队伍状态查询
        int status = Optional.ofNullable(teamQuery.getStatus()).orElse(0);
        TeamStatusEnum teamStatus = TeamStatusEnum.getEnumByValue(status);
        if(teamStatus == null) {
            teamStatus = TeamStatusEnum.PUBLIC;
        }
        // 只有管理员可以查询私有队伍
        if(!isAdmin && teamStatus == TeamStatusEnum.PRIVATE ) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        qw.eq("status", teamStatus.getValue());

        Page<Team> teamPage = this.page(page, qw);

        return teamPage.getRecords().stream()
                .map(team -> {
                    if(team == null) {
                        return null;
                    }
                    TeamVo teamVo = new TeamVo();
                    BeanUtils.copyProperties(team, teamVo);
                    List<User> userList = userTeamMapper.getUserListByTeamId(team.getId());
                    teamVo.setUserList(userList);
                    return teamVo;
                }).toList();
    }


    public boolean updateTeamById(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        boolean isAdmin = userService.isAdmin(loginUser);

        //2. 查询队伍是否存在
        Long id = teamUpdateRequest.getId();
        if(id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍id错误");
        }
        Team team = this.getById(id);
        if(team == null) {
            throw new BusinessException(ErrorCode.TEAM_NOT_EXIST);
        }
        //3. 只有管理员或者队伍的创建者可以修改
        if(!isAdmin && !team.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        //4. 如果用户传入的新值和旧值一致就不用update（降低数据库使用次数）
        //   1. 队伍人数>1且<=20
        Integer maxNum = teamUpdateRequest.getMaxNum();
        if(maxNum != null && (maxNum < 1 || maxNum > 20)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍人数不合法");
        }
        //   2. 队伍标题长度<20
        String name = teamUpdateRequest.getName();
        if(StringUtils.isNotBlank(name) && name.length() >= 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍标题错误");
        }
        //   3. 描述<=512
        String description = teamUpdateRequest.getDescription();
        if(StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍描述错误");
        }
        //  4. status是否为加密
        int status = Optional.ofNullable(teamUpdateRequest.getStatus()).orElse(0);
        String password = teamUpdateRequest.getPassword();
        if (status == TeamStatusEnum.SECRET.getValue() && StringUtils.isBlank(password) || password.length() > 32) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码设置错误");
        }
        // 5. status是否公开,如果是公开的就不需要密码
        if (status == TeamStatusEnum.PUBLIC.getValue()) {
            teamUpdateRequest.setPassword(null);
        }
        BeanUtils.copyProperties(teamUpdateRequest, team);

        //5. 更新成功
        return this.updateById(team);
    }
}




