package com.suave.matematch.model.domain.enums;


public enum TeamValueEnum {
    // 定义枚举值
    PUBLIC (0, "公开"),
    PRIVATE(1, "私有"),
    SECRET(2, "加密");

    private int value;
    private String text;

    TeamValueEnum(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public static TeamValueEnum getEnumByValue(int value) {
        if(value < 0) {
            return null;
        }

        for(TeamValueEnum teamValueEnum : TeamValueEnum.values()) {
            if(teamValueEnum.getValue() == value) {
                return teamValueEnum;
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
