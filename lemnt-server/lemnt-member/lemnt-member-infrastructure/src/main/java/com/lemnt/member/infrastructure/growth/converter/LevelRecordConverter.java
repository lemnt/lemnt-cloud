package com.lemnt.member.infrastructure.growth.converter;

import com.lemnt.member.domain.growth.model.LevelRecord;
import com.lemnt.member.infrastructure.growth.dataobject.LevelRecordDO;

/**
 * 等级记录转换器。
 */
public final class LevelRecordConverter {
    private LevelRecordConverter() {}

    /**
     * 转换为领域对象。
     */
    public static LevelRecord toEntity(LevelRecordDO row) {
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

    /**
     * 转换为持久化数据对象。
     */
    public static LevelRecordDO toDO(LevelRecord value) {
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
