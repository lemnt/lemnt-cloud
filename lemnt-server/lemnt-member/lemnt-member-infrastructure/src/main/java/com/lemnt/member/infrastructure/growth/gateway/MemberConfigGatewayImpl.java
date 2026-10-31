package com.lemnt.member.infrastructure.growth.gateway;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.growth.gateway.MemberConfigGateway;
import com.lemnt.member.domain.growth.model.Config;
import com.lemnt.member.infrastructure.growth.converter.ConfigConverter;
import com.lemnt.member.infrastructure.growth.dataobject.ConfigDO;
import com.lemnt.member.infrastructure.growth.mapper.MemberConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 配置沿源首条记录读写，空表可首次保存，不依赖固定ID。 */
@Component
@RequiredArgsConstructor
public class MemberConfigGatewayImpl implements MemberConfigGateway {
    private final MemberConfigMapper mapper;

    /** 读取实际首条配置，不用ID=1假设替代已有数据。 */
    @Override
    public Config config() {
        return ConfigConverter.toEntity(first());
    }

    /** 外层配置事务锁保护首次插入与更新。 */
    @Override
    public void configSave(Config value) {
        var current = first();
        var row = ConfigConverter.toDO(value);
        if (current == null) {
            mapper.insert(row);
        } else {
            row.setId(current.getId());
            if (mapper.updateById(row) != 1) {
                throw new ServiceException("积分配置已变更，请刷新后重试", 409);
            }
        }
    }

    /** 沿ID升序选取首条，使用Lambda及固定LIMIT。 */
    private ConfigDO first() {
        return mapper.selectOne(Wrappers.<ConfigDO>lambdaQuery().orderByAsc(ConfigDO::getId).last("LIMIT 1"));
    }
}
