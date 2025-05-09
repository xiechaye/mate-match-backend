package com.suave.matematch.model.domain.request;

import com.suave.matematch.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeamQuery extends PageRequest {
    @Serial
    private static final long serialVersionUID = 7249932929071404994L;
    /**
     * id
     */
    private Long id;

    /**
     * 队伍id集合
     */
    private List<Long> idList;

    /**
     * 查询关键字
     */
    private String searchText;

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
}
