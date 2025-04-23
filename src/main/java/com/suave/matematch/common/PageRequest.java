package com.suave.matematch.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用请求类
 */
@Data
public class PageRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -3578262997280815541L;

    /**
     * 当前页码
     */
    protected Integer pageNum = 1;
    /**
     * 每页显示的条数
     */
    protected Integer pageSize = 10;
}
