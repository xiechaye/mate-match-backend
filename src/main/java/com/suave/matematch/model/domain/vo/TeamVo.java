package com.suave.matematch.model.domain.vo;

import com.suave.matematch.model.domain.User;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author Suave
 */
@Data
public class TeamVo {
    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 队伍描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建者id
     */
    private Long userId;

    /**
     *  0-公开， 1-私有， 2-加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 队伍成员信息
     */
    private List<User> userList;
}
