package com.suave.matematch.contant;

/**
 * RedisKey常量前缀
 *
 * @author Suave
 */
public interface RedisConstant {

    /**
     * 推荐默认用户功能redisKey前缀
      */
    String RECOMMEND_KEY = "matematch:recommend";

    /**
     * 定时任务预热登录用户推荐redisKey前缀
     */
    String SCHEDULED_LOGIN_KEY = "matematch:precahejob:docache:lock";

    /**
     * 定时任务预热未登录用户推荐redisKey前缀
     */
    String SCHEDULED_UNLOGIN_KEY = "matematch:precahejob:dounlogincache:lock";

    /**
     * 匹配伙伴功能redisKey前缀
     */
    String MATE_MATCH_KEY = "matematch:matchUser";

    /**
     * 添加队伍功能redisKey前缀
     */
    String ADD_TEAM_KEY = "matematch:addTeam:lock";

    /**
     * 加入队伍功能redisKey前缀
     */
    String JOIN_TEAM_KEY = "matematch:joinTeam:lock";

}
