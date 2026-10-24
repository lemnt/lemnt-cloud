package com.lemnt.common.security.exception;

import org.apache.commons.lang3.StringUtils;

import java.io.Serial;

/**
 * 未能通过的权限认证异常
 *
 * @author wangxx
 */
public class NotPermissionException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public NotPermissionException(String permission) {
        super(permission);
    }

    public NotPermissionException(String[] permissions) {
        super(StringUtils.join(permissions, ","));
    }
}
