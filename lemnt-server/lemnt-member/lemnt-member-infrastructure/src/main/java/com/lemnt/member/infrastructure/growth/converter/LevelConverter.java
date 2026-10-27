package com.lemnt.member.infrastructure.growth.converter;

import com.lemnt.member.domain.growth.model.Level;
import com.lemnt.member.infrastructure.growth.dataobject.LevelDO;

/**
 * 等级转换器。
 */
public final class LevelConverter {
    private LevelConverter() {}

    /**
     * 转换为领域对象。
     */
    public static Level toEntity(LevelDO row) {
        return row == null
                ? null
                : new Level(
                        row.getId(),
                        row.getName(),
                        row.getLevel(),
                        row.getExperience(),
                        row.getDiscountPercent(),
                        row.getIcon(),
                        row.getBackgroundUrl(),
                        row.getStatus());
    }

    /**
     * 转换为持久化数据对象。
     */
    public static LevelDO toDO(Level value) {
        var row = new LevelDO();
        row.setId(value.id());
        row.setName(value.name());
        row.setLevel(value.level());
        row.setExperience(value.experience());
        row.setDiscountPercent(value.discountPercent());
        row.setIcon(value.icon());
        row.setBackgroundUrl(value.backgroundUrl());
        row.setStatus(value.status());
        return row;
    }
}
