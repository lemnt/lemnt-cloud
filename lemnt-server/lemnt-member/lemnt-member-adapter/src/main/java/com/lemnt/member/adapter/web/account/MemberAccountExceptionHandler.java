package com.lemnt.member.adapter.web.account;

import com.lemnt.common.core.domain.R;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(basePackages = "com.lemnt.member.adapter.web.account")
public class MemberAccountExceptionHandler {
    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.UNAUTHORIZED)
    public R<Void> unauthorized(SecurityException ex) {
        return R.fail(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST)
    public R<Void> invalid(IllegalArgumentException ex) {
        return R.fail(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.CONFLICT)
    public R<Void> conflict(IllegalStateException ex) {
        return R.fail(ex.getMessage());
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
    public R<Void> unavailable() {
        return R.fail("所需会员外部服务尚未配置");
    }

    @ExceptionHandler(com.lemnt.member.domain.account.model.MemberProviderException.class)
    @ResponseStatus(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)
    public R<Void> providerUnavailable() {
        return R.fail("会员外部服务暂不可用");
    }
}
