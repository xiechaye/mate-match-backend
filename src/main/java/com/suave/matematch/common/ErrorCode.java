package com.suave.matematch.common;

/**
 * 错误码
 *
 */
public enum ErrorCode {

    SUCCESS(0, "ok", ""),
    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "请求数据为空", ""),
    NOT_LOGIN(40100, "未登录", ""),
    NO_AUTH(40101, "无权限", ""),
    USER_NOT_EXIST(40102, "用户不存在", ""),
    TEAM_NOT_EXIST(40201, "队伍不存在", ""),
    TEAM_FILLED(40202, "队伍已满", ""),
    TEAM_EXPIRED(40203, "队伍已过期", ""),
    TEAM_USER_EXIST(40204, "用户已在队伍中", ""),
    TEAM_PRIVATE(40205, "队伍为私密队伍", ""),
    SYSTEM_ERROR(50000, "系统内部异常", "");


    private final int code;

    /**
     * 状态码信息
     */
    private final String message;

    /**
     * 状态码描述（详情）
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    // https://t.zsxq.com/0emozsIJh

    public String getDescription() {
        return description;
    }
}
