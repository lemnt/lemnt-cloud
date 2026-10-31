package com.lemnt.member.infrastructure.growth.gateway;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.growth.gateway.MemberSignConfigGateway;
import com.lemnt.member.domain.growth.model.SignConfig;
import com.lemnt.member.infrastructure.growth.converter.SignConfigConverter;
import com.lemnt.member.infrastructure.growth.dataobject.SignConfigDO;
import com.lemnt.member.infrastructure.growth.mapper.MemberSignConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.List;

/** 签到配置按ID管理，数据库唯一约束兜底并发天数冲突。 */
@Component
@RequiredArgsConstructor
public class MemberSignConfigGatewayImpl implements MemberSignConfigGateway {
    private final MemberSignConfigMapper mapper;
    @Override
    public List<SignConfig> signConfigs() {
        return mapper.selectList(Wrappers.<SignConfigDO>lambdaQuery().orderByAsc(SignConfigDO::getDay))
                .stream().map(SignConfigConverter::toEntity).toList();
    }
    @Override
    public SignConfig signConfig(Long id) {
        return SignConfigConverter.toEntity(mapper.selectById(id));
    }
    @Override
    public SignConfig signConfigByDay(Integer day) {
        return SignConfigConverter.toEntity(mapper.selectOne(Wrappers.<SignConfigDO>lambdaQuery().eq(SignConfigDO::getDay, day)));
    }
    @Override
    public Long create(SignConfig value) {
        var row = SignConfigConverter.toDO(value);
        try {
            mapper.insert(row);
        } catch (DuplicateKeyException exception) {
            throw new ServiceException("签到天数已存在", 409);
        }
        return row.getId();
    }
    @Override
    public boolean update(SignConfig value) {
        try {
            return mapper.updateById(SignConfigConverter.toDO(value)) == 1;
        } catch (DuplicateKeyException exception) {
            throw new ServiceException("签到天数已存在", 409);
        }
    }
    @Override
    public boolean signConfigDelete(Long id) {
        return mapper.deleteById(id) == 1;
    }
}
