package com.lemnt.member.infrastructure.growth.converter;

import com.lemnt.member.domain.growth.model.SignConfig;
import com.lemnt.member.infrastructure.growth.dataobject.SignConfigDO;

/**
 * 签到配置转换器。
 */
public final class SignConfigConverter {
    private SignConfigConverter() {}

    /**
     * 转换为领域对象。
     */
    public static SignConfig toEntity(SignConfigDO row) {
        return row == null
                ? null
                : new SignConfig(
                        row.getId(), row.getDay(), row.getPoint(), row.getExperience(), row.getStatus());
    }

    /**
     * 转换为持久化数据对象。
     */
    public static SignConfigDO toDO(SignConfig value) {
        var row = new SignConfigDO();
        row.setId(value.id());
        row.setDay(value.day());
        row.setPoint(value.point());
        row.setExperience(value.experience());
        row.setStatus(value.status());
        return row;
    }
}
