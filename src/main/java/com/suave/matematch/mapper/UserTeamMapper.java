package com.suave.matematch.mapper;

import com.suave.matematch.model.domain.User;
import com.suave.matematch.model.domain.UserTeam;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户队伍 Mapper
 *
 * @author Suave
 */
@Mapper
public interface UserTeamMapper extends BaseMapper<UserTeam> {

    public List<User> getUserListByTeamId(Long teamId);

    Long getSecondUserIdByTeamId(Long teamId);
}




