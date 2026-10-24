package com.lemnt.auth.domain.log.enums;

import lombok.Getter;

/**
 * 登录方式
 *
 */
@Getter
public enum LoginMethodEnum {
    PASSWORD(1, "账号密码"),
    ;

    private final Integer code;
    private final String message;

    LoginMethodEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
