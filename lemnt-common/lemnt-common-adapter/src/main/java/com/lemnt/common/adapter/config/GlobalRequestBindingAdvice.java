package com.lemnt.common.adapter.config;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.support.ExtendedServletRequestDataBinder;

/**
 * 请求头仅作为显式 Header 入参或登录上下文，不自动绑定到业务请求对象。
 */
@ControllerAdvice
public class GlobalRequestBindingAdvice {

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        if (binder instanceof ExtendedServletRequestDataBinder servletBinder) {
            servletBinder.setHeaderPredicate(header -> false);
        }
    }
}
