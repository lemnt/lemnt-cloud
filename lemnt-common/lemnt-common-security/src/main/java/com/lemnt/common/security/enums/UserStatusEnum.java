package com.lemnt.common.security.enums;

import lombok.Getter;

/**
 * 用户状态
 *
 * @author wangxx
 */
@Getter
public enum UserStatusEnum {
    OK("1", "正常"),
    DISABLE("2", "停用");

    private final String code;
    private final String info;

    UserStatusEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }
}
