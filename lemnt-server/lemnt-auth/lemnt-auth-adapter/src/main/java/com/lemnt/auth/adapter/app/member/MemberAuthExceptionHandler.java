package com.lemnt.auth.adapter.app.member;

import com.lemnt.common.core.domain.R;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 小程序认证业务异常沿用原会员接口状态码，不影响后台认证。
 */
@Order(0)
@RestControllerAdvice(basePackageClasses = AppMemberAuthController.class)
public class MemberAuthExceptionHandler {
    /**
     * 认证失败返回401。
     */
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<Void> unauthorized(SecurityException ex) {
        return R.fail(ex.getMessage());
    }

    /**
     * 非法输入返回400。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public R<Void> invalid(IllegalArgumentException ex) {
        return R.fail(ex.getMessage());
    }

    /**
     * 业务状态冲突返回409。
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public R<Void> conflict(IllegalStateException ex) {
        return R.fail(ex.getMessage());
    }
}
