package com.swimmingliu.common.enums;

public enum ChatTypeEnum {
    CHAT("普通对话"),
    REASON("推理对话"),
    CHAT_WEB("普通对话-网络搜索"),
    REASON_WEB("推理对话-网络搜索");

    private final String desc;

    ChatTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}