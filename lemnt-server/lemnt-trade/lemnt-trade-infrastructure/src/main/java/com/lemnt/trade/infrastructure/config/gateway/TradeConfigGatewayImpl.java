package com.lemnt.trade.infrastructure.config.gateway;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.api.BusinessConfigKeys;
import com.lemnt.system.api.RemoteConfigService;
import com.lemnt.system.api.bo.BusinessConfigSaveBo;
import com.lemnt.trade.domain.config.gateway.TradeConfigGateway;
import com.lemnt.trade.domain.config.model.TradeConfig;

import com.alibaba.fastjson2.JSON;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 通过system保存整组业务配置，不访问旧表或跨服务数据库。 */
@Component
public class TradeConfigGatewayImpl implements TradeConfigGateway {
    @DubboReference(check = false, retries = 0)
    private RemoteConfigService remoteConfigService;

    /** 单次读取完整JSON，保持关联参数来自同一版本；RPC故障不降级为默认配置。 */
    @Override
    public TradeConfig get() {
        String value = remoteConfigService.getConfig(BusinessConfigKeys.TRADE);
        return value == null ? null : JSON.parseObject(value, TradeConfig.class);
    }

    /** 业务校验由原应用服务完成，system以唯一键原子替换整个配置组。 */
    @Override
    public void update(TradeConfig value) {
        remoteConfigService.saveBusinessConfig(
                new BusinessConfigSaveBo(
                        BusinessConfigKeys.TRADE,
                        JSON.toJSONString(value),
                        SecurityUtils.getUsername()));
    }
}
