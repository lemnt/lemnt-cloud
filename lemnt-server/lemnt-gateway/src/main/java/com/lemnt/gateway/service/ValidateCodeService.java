package com.lemnt.gateway.service;

import java.io.IOException;
import java.util.Map;

import com.lemnt.common.core.domain.R;
import com.lemnt.common.security.exception.CaptchaException;

/**
 * 验证码处理
 *
 * @author wangxx
 */
public interface ValidateCodeService {
    /**
     * 生成验证码
     */
    R<Map<String, Object>> createCaptcha() throws IOException, CaptchaException;

    /**
     * 校验验证码
     */
    void checkCaptcha(String key, String value) throws CaptchaException;
}
