package com.lemnt.member.infrastructure.account.converter;

import com.lemnt.member.domain.account.model.LevelRecord;
import com.lemnt.member.infrastructure.account.dataobject.LevelRecordDO;

public final class LevelRecordConverter {
    private LevelRecordConverter() {}

    public static LevelRecord domain(LevelRecordDO row) {
        return row == null
                ? null
                : new LevelRecord(
                        row.getId(),
                        row.getMemberId(),
                        row.getLevelId(),
                        row.getExperience(),
                        row.getReason(),
                        row.getCreateTime());
    }

    public static LevelRecordDO row(LevelRecord value) {
        var row = new LevelRecordDO();
        row.setId(value.id());
        row.setMemberId(value.memberId());
        row.setLevelId(value.levelId());
        row.setExperience(value.experience());
        row.setReason(value.reason());
        row.setCreateTime(value.createTime());
        return row;
    }
}
