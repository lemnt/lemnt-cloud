package com.lemnt.common.security.enums;

import lombok.Getter;

/**
 * 状态枚举
 *
 * @author wangxx
 */
@Getter
public enum StatusEnum {

    NORMAL(1, "启用"),
    DISABLE(2, "禁用"),
    ;

    private final Integer state;
    private final String value;

    StatusEnum(Integer state, String value) {
        this.state = state;
        this.value = value;
    }
}
