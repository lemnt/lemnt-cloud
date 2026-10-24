package com.lemnt.common.datascope.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * @author wangxx
 * @version 1.0.0
 * @date 2025/7/28 21:56
 */
@Getter
public enum DataScopeEnum {

    /**
     * 全部数据权限
     */
    ALL("1"),

    /**
     * 自定数据权限
     */
    CUSTOM("2"),

    /**
     * 部门数据权限
     */
    DEPT("3"),

    /**
     * 部门及以下数据权限
     */
    DEPT_AND_CHILD("4"),

    /**
     * 仅本人数据权限
     */
    SELF("5");

    /**
     * 范围
     */
    private final String scope;

    DataScopeEnum(String scope) {
        this.scope = scope;
    }

    public static DataScopeEnum getDataScopeEnum(String scope) {
        for (DataScopeEnum dataScopeEnum : DataScopeEnum.values()) {
            if (Objects.equals(dataScopeEnum.getScope(), scope)) {
                return dataScopeEnum;
            }
        }
        return null;
    }
}
