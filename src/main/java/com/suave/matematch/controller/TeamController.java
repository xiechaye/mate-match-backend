package com.suave.matematch.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.suave.matematch.common.BaseResponse;
import com.suave.matematch.common.ErrorCode;
import com.suave.matematch.common.ResultUtils;
import com.suave.matematch.exception.BusinessException;
import com.suave.matematch.model.domain.Team;
import com.suave.matematch.model.domain.User;
import com.suave.matematch.model.domain.UserTeam;
import com.suave.matematch.model.domain.request.TeamAddRequest;
import com.suave.matematch.model.domain.request.TeamJoinRequest;
import com.suave.matematch.model.domain.request.TeamQuery;
import com.suave.matematch.model.domain.request.TeamUpdateRequest;
import com.suave.matematch.model.domain.vo.TeamVo;
import com.suave.matematch.service.TeamService;
import com.suave.matematch.service.UserService;
import com.suave.matematch.service.UserTeamService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/team")
@AllArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:80",
        "http://120.26.19.193"
},  allowCredentials = "true")
public class TeamController {

    private final TeamService teamService;
    private final UserService userService;
    private final UserTeamService userTeamService;

    /**
     * 添加队伍
     * @param teamAddRequest 队伍信息
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest,
                                      HttpServletRequest request) {
        if(teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 将请求参数转换为实体类
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);

        // 获取登录用户的信息
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        team.setUserId(loginUser.getId());

        Long id = teamService.addTeam(team);
        return ResultUtils.success(id);
    }

    /**
     * 更新队伍
     * @param teamUpdateRequest 队伍信息
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest,
                                            HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if(teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean update = teamService.updateTeamById(teamUpdateRequest, loginUser);
        return ResultUtils.success(update);
    }

    /**
     * 查询队伍
     * @param id 队伍id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Team> getTeam(@RequestParam Long id) {
        if(id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if(team == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取队伍失败");
        }
        return ResultUtils.success(team);
    }

    /**
     * 查询队伍列表
     * @param teamQuery 队伍查询参数
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamVo>> getTeamList(TeamQuery teamQuery, HttpServletRequest request) {
        //1. 判断请求参数是否为空
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean isAdmin = userService.isAdmin(request);


        List<TeamVo> teamVoList = teamService.getTeamList(teamQuery, isAdmin);
        return ResultUtils.success(teamVoList);
    }

    /**
     * 加入队伍
     * @param teamJoinRequest 队伍加入参数
     * @param request
     * @return
     */
    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,
                                          HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        // 判断请求参数是否为空
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Boolean joined = teamService.joinTeam(teamJoinRequest, loginUser);

        return ResultUtils.success(joined);
    }

    /**
     * 退出队伍
     * @param teamId 队伍id
     * @return
     */
    @DeleteMapping("/quit/{teamId}")
    public BaseResponse<Boolean> quitTeam(@PathVariable Long teamId, HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        if(teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean quited = teamService.quitTeam(teamId, loginUser.getId());
        return ResultUtils.success(quited);
    }

    /**
     * 删除队伍
     * @param teamId 队伍id
     * @return
     */
    @DeleteMapping("/delete/{teamId}")
    public BaseResponse<Boolean> deleteTeam(@PathVariable Long teamId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        if(teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean deleted = teamService.deleteTeam(teamId, loginUser.getId());
        return ResultUtils.success(deleted);
    }

    /**
     * 查询个人创建队伍列表
     * @param teamQuery 队伍查询参数
     * @return
     */
    @GetMapping("/list/create")
    public BaseResponse<List<TeamVo>> getMyTeamList(TeamQuery teamQuery, HttpServletRequest request) {
        //1. 判断请求参数是否为空
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        teamQuery.setUserId(loginUser.getId());
        List<TeamVo> teamVoList = teamService.getTeamList(teamQuery, true);
        return ResultUtils.success(teamVoList);
    }

    /**
     * 查询个人加入队伍列表
     * @param teamQuery 队伍查询参数
     * @return
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamVo>> getMyJoinTeamList(TeamQuery teamQuery, HttpServletRequest request) {
        //1. 判断请求参数是否为空
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 查询个人的用户队伍信息
        QueryWrapper<UserTeam> qw = new QueryWrapper<>();
        qw.eq("userId", loginUser.getId());
        List<UserTeam> list = userTeamService.list(qw);

        // 获取队伍id
        Map<Long, List<UserTeam>> collect = list.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(collect.keySet());
        teamQuery.setIdList(idList);

        List<TeamVo> teamVoList = teamService.getTeamList(teamQuery, true);
        return ResultUtils.success(teamVoList);
    }
}
