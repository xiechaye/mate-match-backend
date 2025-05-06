package com.suave.matematch.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.suave.matematch.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.suave.matematch.model.domain.User;
import com.suave.matematch.model.domain.request.TeamJoinRequest;
import com.suave.matematch.model.domain.request.TeamQuery;
import com.suave.matematch.model.domain.request.TeamUpdateRequest;
import com.suave.matematch.model.domain.vo.TeamVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * @author Suave
 */
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team 队伍信息
     * @return
     */
    Long addTeam(Team team);

    /**
     * 查询队伍列表
     * @param teamQuery 队伍信息
     * @param isAdmin 是否是管理员
     * @return
     */
    List<TeamVo> getTeamList(TeamQuery teamQuery, Boolean isAdmin);

    /**
     * 查询队伍详情
     * @param teamUpdateRequest 队伍信息
     * @param loginUser 登录用户
     * @return
     */
    boolean updateTeamById(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 用户加入队伍
     * @param teamJoinRequest 队伍信息
     * @param loginUser 登录用户
     * @return
     */
    Boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 用户退出队伍
     * @param teamId 队伍id
     * @param userId 用户id
     * @return
     */
    boolean quitTeam(Long teamId, Long userId);

    /**
     * 队长解散队伍
     * @param teamId 队伍id
     * @param userId 用户id
     * @return
     */
    boolean deleteTeam(Long teamId, Long userId);
}
