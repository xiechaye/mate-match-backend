package com.suave.matematch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.suave.matematch.common.ErrorCode;
import com.suave.matematch.exception.BusinessException;
import com.suave.matematch.model.domain.Team;
import com.suave.matematch.model.domain.UserTeam;
import com.suave.matematch.model.domain.enums.TeamValueEnum;
import com.suave.matematch.service.TeamService;
import com.suave.matematch.mapper.TeamMapper;
import com.suave.matematch.service.UserTeamService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

/**
* @author Suave
*/
@Service
@AllArgsConstructor
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService{
    private final UserTeamService userTeamService;

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
        TeamValueEnum teamValueEnum = TeamValueEnum.getEnumByValue(status);
        if(teamValueEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍状态错误");
        }
        //   5. 如果status是加密状态，一定要有密码，且密码<=32
        String password = team.getPassword();
        if (status == TeamValueEnum.SECRET.getValue() && StringUtils.isBlank(password) || password.length() > 32) {
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
}




