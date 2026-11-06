package com.lemnt.member.domain.growth.gateway;

import com.lemnt.member.domain.growth.model.Config;

/**
 * 积分配置组领域存储网关。
 */
public interface MemberConfigGateway {

    /**
     * 配置。
     */
    Config config();

    /**
     * 配置保存。
     */
    void configSave(Config value);
}
