package com.lemnt.system.api;

/** 只能经原业务接口维护的完整配置组，防止通用参数入口绕过校验。 */
public final class BusinessConfigKeys {
    public static final String MEMBER_POINT = "member.point.config";
    public static final String TRADE = "trade.config";

    private BusinessConfigKeys() { }

    public static boolean reserved(String key) {
        return key != null && (MEMBER_POINT.equalsIgnoreCase(key.strip()) || TRADE.equalsIgnoreCase(key.strip()));
    }
}
