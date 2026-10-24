package com.lemnt.common.log.enums;

/**
 * 操作状态
 *
 * @author wangxx
 *
 */
public enum BusinessStatus {
    /**
     * 成功
     */
    SUCCESS(1),

    /**
     * 失败
     */
    FAIL(2);

    private final int code;

    BusinessStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
