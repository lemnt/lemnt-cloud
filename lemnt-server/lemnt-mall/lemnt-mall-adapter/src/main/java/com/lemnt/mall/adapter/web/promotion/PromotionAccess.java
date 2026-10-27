package com.lemnt.mall.adapter.web.promotion;

import com.lemnt.common.security.auth.AuthUtil;
import com.lemnt.common.security.exception.NotLoginException;
import com.lemnt.common.security.utils.SecurityUtils;

import org.springframework.stereotype.Component;

/**
 * Uses the authenticated admin SecurityUser, never caller-provided identity headers.
 */
@Component
public class PromotionAccess {
    /**
     * 操作人。
     */
    public long operator(String permission) {
        if (AuthUtil.authLogic == null) {
            throw new NotLoginException("Administrator session required");
        }
        AuthUtil.checkLogin();
        var user = SecurityUtils.getLoginUser();
        if (user == null
                || user.getUserId() == null
                || user.getUserId() <= 0
                || user.getExpireTime() == null
                || user.getExpireTime() <= System.currentTimeMillis()) {
            throw new NotLoginException("Administrator session required");
        }
        AuthUtil.checkPermi(permission);
        return user.getUserId();
    }
}
