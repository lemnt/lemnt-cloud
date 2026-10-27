package com.lemnt.member.infrastructure.growth.gateway;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.growth.gateway.MemberConfigGateway;
import com.lemnt.member.domain.growth.model.Config;
import com.lemnt.member.infrastructure.growth.converter.ConfigConverter;
import com.lemnt.member.infrastructure.growth.mapper.MemberConfigMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** member_config 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class MemberConfigGatewayImpl implements MemberConfigGateway {

    private final MemberConfigMapper memberConfigMapper;

    /**
     * 锁定会员配置行，串行化目录相关变更。
     */
    @Override
    public void lockCatalog() {
        if (memberConfigMapper.lockCatalog() == null) {
            throw new ServiceException("会员配置缺失", 409);
        }
    }

    /**
     * 配置。
     */
    @Override
    public Config config() {
        return ConfigConverter.toEntity(memberConfigMapper.config());
    }

    /**
     * 配置保存。
     */
    @Override
    public void configSave(Config value) {
        memberConfigMapper.configSave(ConfigConverter.toDO(value));
    }
}
