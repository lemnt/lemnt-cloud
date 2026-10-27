package com.lemnt.member.domain.growth.gateway;

import com.lemnt.member.domain.growth.model.SignConfig;

import java.util.List;

/** member_sign_config 表领域存储网关。 */
public interface MemberSignConfigGateway {

    /**
     * 签到配置项。
     */
    List<SignConfig> signConfigs();

    /**
     * 签到配置保存。
     */
    void signConfigSave(SignConfig value);

    /**
     * 签到配置删除。
     */
    boolean signConfigDelete(Integer day);
}
