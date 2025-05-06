package com.suave.matematch.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.suave.matematch.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.suave.matematch.model.domain.request.TeamQuery;
import com.suave.matematch.model.domain.vo.TeamVo;

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
     * @param page 分页参数
     * @return
     */
    List<TeamVo> getTeamList(TeamQuery teamQuery, Page<Team> page, Boolean isAdmin);
}
