package com.suave.matematch.service;

import com.suave.matematch.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
