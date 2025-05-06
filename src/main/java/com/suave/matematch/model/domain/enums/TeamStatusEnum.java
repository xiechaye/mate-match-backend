package com.suave.matematch.model.domain.enums;


public enum TeamStatusEnum {
    // 定义枚举值
    PUBLIC (0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");

    private int value;
    private String text;

    TeamStatusEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public static TeamStatusEnum getEnumByValue(int value) {
        if(value < 0) {
            return null;
        }

        for(TeamStatusEnum teamStatusEnum : TeamStatusEnum.values()) {
            if(teamStatusEnum.getValue() == value) {
                return teamStatusEnum;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String gettext() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
