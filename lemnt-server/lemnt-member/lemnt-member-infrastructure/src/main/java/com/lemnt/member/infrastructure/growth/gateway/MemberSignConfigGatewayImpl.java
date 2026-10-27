package com.lemnt.member.infrastructure.growth.gateway;

import com.lemnt.member.domain.growth.gateway.MemberSignConfigGateway;
import com.lemnt.member.domain.growth.model.SignConfig;
import com.lemnt.member.infrastructure.growth.converter.SignConfigConverter;
import com.lemnt.member.infrastructure.growth.mapper.MemberSignConfigMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** member_sign_config 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class MemberSignConfigGatewayImpl implements MemberSignConfigGateway {

    private final MemberSignConfigMapper memberSignConfigMapper;

    /**
     * 签到配置项。
     */
    @Override
    public List<SignConfig> signConfigs() {
        return memberSignConfigMapper.signConfigs().stream().map(SignConfigConverter::toEntity).toList();
    }

    /**
     * 签到配置保存。
     */
    @Override
    public void signConfigSave(SignConfig value) {
        memberSignConfigMapper.signConfigSave(SignConfigConverter.toDO(value));
    }

    /**
     * 签到配置删除。
     */
    @Override
    public boolean signConfigDelete(Integer day) {
        return memberSignConfigMapper.signConfigDelete(day) == 1;
    }
}
