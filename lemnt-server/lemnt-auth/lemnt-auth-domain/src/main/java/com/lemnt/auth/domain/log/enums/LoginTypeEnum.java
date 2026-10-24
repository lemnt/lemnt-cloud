package com.lemnt.auth.domain.log.enums;

import lombok.Getter;

/**
 * 登录类型
 *
 */
@Getter
public enum LoginTypeEnum {
    LOGIN(1, "登录"),
    LOGIN_OUT(2, "登出"),
    REGISTER(3, "注册"),
    ;

    private final Integer code;
    private final String message;

    LoginTypeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
