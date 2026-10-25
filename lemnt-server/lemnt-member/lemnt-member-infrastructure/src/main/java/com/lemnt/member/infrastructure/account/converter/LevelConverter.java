package com.lemnt.member.infrastructure.account.converter;

import com.lemnt.member.domain.account.model.Level;
import com.lemnt.member.infrastructure.account.dataobject.LevelDO;

public final class LevelConverter {
    private LevelConverter() {}

    public static Level domain(LevelDO row) {
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

    public static LevelDO row(Level value) {
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
