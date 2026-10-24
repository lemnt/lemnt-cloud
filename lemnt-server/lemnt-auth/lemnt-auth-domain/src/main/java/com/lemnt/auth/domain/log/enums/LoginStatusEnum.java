package com.lemnt.auth.domain.log.enums;

import lombok.Getter;

/**
 * 登录日志状态
 *
 */
@Getter
public enum LoginStatusEnum {
    SUCCESS("1", "成功"),
    FAIL("2", "失败"),
    ;

    private final String code;
    private final String message;

    LoginStatusEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
