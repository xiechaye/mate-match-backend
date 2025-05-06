package com.suave.matematch.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.suave.matematch.common.BaseResponse;
import com.suave.matematch.common.ErrorCode;
import com.suave.matematch.common.ResultUtils;
import com.suave.matematch.exception.BusinessException;
import com.suave.matematch.model.domain.Team;
import com.suave.matematch.model.domain.User;
import com.suave.matematch.model.domain.request.TeamAddRequest;
import com.suave.matematch.model.domain.request.TeamJoinRequest;
import com.suave.matematch.model.domain.request.TeamQuery;
import com.suave.matematch.model.domain.request.TeamUpdateRequest;
import com.suave.matematch.model.domain.vo.TeamVo;
import com.suave.matematch.service.TeamService;
import com.suave.matematch.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/team")
@AllArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173"}, allowCredentials = "true")
public class TeamController {

    private final TeamService teamService;
    private final UserService userService;

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
     * 删除队伍
     * @param id 队伍id
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody Long id) {
        if(id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean remove = teamService.removeById(id);
        if(!remove) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败");
        }
        return ResultUtils.success(true);
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
        if(!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍失败");
        }
        return ResultUtils.success(true);
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
}
