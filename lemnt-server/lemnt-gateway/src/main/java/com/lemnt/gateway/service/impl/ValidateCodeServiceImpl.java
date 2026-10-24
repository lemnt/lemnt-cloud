package com.lemnt.gateway.service.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import cn.hutool.core.codec.Base64;
import com.lemnt.common.core.domain.R;
import com.lemnt.common.redis.service.RedisStringService;
import com.lemnt.common.snowflake.utils.SnowflakeUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FastByteArrayOutputStream;
import com.google.code.kaptcha.Producer;
import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.core.constant.Constants;
import com.lemnt.common.security.exception.CaptchaException;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.gateway.config.properties.CaptchaProperties;
import com.lemnt.gateway.service.ValidateCodeService;

/**
 * 验证码实现处理
 *
 * @author wangxx
 */
@Service
public class ValidateCodeServiceImpl implements ValidateCodeService {

    @Resource(name = "captchaProducer")
    private Producer captchaProducer;
    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;
    @Resource
    private RedisStringService redisStringService;
    @Resource
    private CaptchaProperties captchaProperties;
    @Resource
    private SnowflakeUtils snowflakeUtils;

    /**
     * 生成验证码
     */
    @Override
    public R<Map<String, Object>> createCaptcha() throws CaptchaException {
        Map<String, Object> dataMap = new HashMap<>();
        boolean captchaEnabled = captchaProperties.getEnabled();
        dataMap.put("captchaEnabled", captchaEnabled);
        if (!captchaEnabled) {
            return R.ok(dataMap);
        }

        // 保存验证码信息
        String uuid = snowflakeUtils.generateStr();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;

        String capStr, code = null;
        BufferedImage image = null;

        String captchaType = captchaProperties.getType();
        // 生成验证码
        if ("math".equals(captchaType)) {
            String capText = captchaProducerMath.createText();
            capStr = capText.substring(0, capText.lastIndexOf("@"));
            code = capText.substring(capText.lastIndexOf("@") + 1);
            image = captchaProducerMath.createImage(capStr);
        } else if ("char".equals(captchaType)) {
            capStr = code = captchaProducer.createText();
            image = captchaProducer.createImage(capStr);
        }

        redisStringService.set(verifyKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        // 转换流信息写出
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", os);
        } catch (IOException e) {
            return R.fail(e.getMessage());
        }

        dataMap.put("uuid", uuid);
        dataMap.put("img", Base64.encode(os.toByteArray()));
        return R.ok(dataMap);
    }

    /**
     * 校验验证码
     */
    @Override
    public void checkCaptcha(String code, String uuid) throws CaptchaException {
        if (StringUtils.isEmpty(code)) {
            throw new CaptchaException("验证码不能为空");
        }
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisStringService.get(verifyKey);
        if (captcha == null) {
            throw new CaptchaException("验证码已失效");
        }
        redisStringService.delete(verifyKey);
        if (!code.equalsIgnoreCase(captcha)) {
            throw new CaptchaException("验证码错误");
        }
    }
}
