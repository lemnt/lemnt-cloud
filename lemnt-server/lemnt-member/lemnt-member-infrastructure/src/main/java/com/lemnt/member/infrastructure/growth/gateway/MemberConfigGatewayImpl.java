package com.lemnt.member.infrastructure.growth.gateway;

import com.alibaba.fastjson2.JSON;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.api.BusinessConfigKeys;
import com.lemnt.system.api.RemoteConfigService;
import com.lemnt.system.api.bo.BusinessConfigSaveBo;
import com.lemnt.member.domain.growth.gateway.MemberConfigGateway;
import com.lemnt.member.domain.growth.model.Config;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 通过system保存整组业务配置，不访问旧表或跨服务数据库。 */
@Component
public class MemberConfigGatewayImpl implements MemberConfigGateway {
    @DubboReference(check = false, retries = 0)
    private RemoteConfigService remoteConfigService;

    /** 单次读取完整JSON，保持关联参数来自同一版本；RPC故障不降级为默认配置。 */
    @Override
    public Config config() {
        String value = remoteConfigService.getConfig(BusinessConfigKeys.MEMBER_POINT);
        return value == null ? null : JSON.parseObject(value, Config.class);
    }

    /** 业务校验由原应用服务完成，system以唯一键原子替换整个配置组。 */
    @Override
    public void configSave(Config value) {
        remoteConfigService.saveBusinessConfig(new BusinessConfigSaveBo(BusinessConfigKeys.MEMBER_POINT,
                JSON.toJSONString(value), SecurityUtils.getUsername()));
    }
}
