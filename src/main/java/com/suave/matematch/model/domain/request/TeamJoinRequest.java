package com.suave.matematch.model.domain.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
public class TeamJoinRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 3636391500174624711L;
    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}
