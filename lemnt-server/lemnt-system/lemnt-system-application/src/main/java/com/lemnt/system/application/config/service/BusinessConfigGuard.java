package com.lemnt.system.application.config.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.system.api.BusinessConfigKeys;

/**
 * 保留配置的业务规则只能由原业务入口校验，通用配置页面只读。
 */
public final class BusinessConfigGuard {
    private BusinessConfigGuard() {
    }

    public static void requireEditable(String key) {
        if (BusinessConfigKeys.reserved(key)) {
            throw new ServiceException("业务配置请通过积分或交易配置页面维护", 409);
        }
    }
}
