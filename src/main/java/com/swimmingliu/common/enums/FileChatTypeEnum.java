package com.swimmingliu.common.enums;

public enum FileChatTypeEnum {
    CHAT("普通对话"),
    REASON("推理对话");

    private final String desc;

    FileChatTypeEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}