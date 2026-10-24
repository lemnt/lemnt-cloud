package com.lemnt.common.sensitive.utils;

import com.lemnt.common.core.utils.StringUtils;

/**
 * 脱敏工具类
 *
 * @author wangxx
 */
public class DesensitizedUtil {
    /**
     * 密码的全部字符都用*代替，比如：******
     *
     * @param password 密码
     * @return 脱敏后的密码
     */
    public static String password(String password) {
        if (StringUtils.isBlank(password)) {
            return StringUtils.EMPTY;
        }
        return StringUtils.repeat('*', password.length());
    }

    /**
     * 车牌中间用*代替，如果是错误的车牌，不处理
     *
     * @param carLicense 完整的车牌号
     * @return 脱敏后的车牌
     */
    public static String carLicense(String carLicense) {
        if (StringUtils.isBlank(carLicense)) {
            return StringUtils.EMPTY;
        }
        // 普通车牌
        if (carLicense.length() == 7) {
            carLicense = StringUtils.hide(carLicense, 3, 6);
        } else if (carLicense.length() == 8) {
            // 新能源车牌
            carLicense = StringUtils.hide(carLicense, 3, 7);
        }
        return carLicense;
    }

    /**
     * 手机号脱敏
     *
     * @param phone 完整的手机号
     * @return 脱敏后的手机号
     */
    public static String phone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return StringUtils.EMPTY;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
}
