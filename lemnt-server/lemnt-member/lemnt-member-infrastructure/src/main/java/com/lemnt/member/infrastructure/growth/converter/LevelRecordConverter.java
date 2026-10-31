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
                        row.getUserId(),
                        row.getLevelId(),
                        row.getUserExperience(),
                        row.getRemark(),
                        row.getCreateTime(),
                        row.getLevel(),
                        row.getDiscountPercent(),
                        row.getExperience(),
                        row.getDescription());
    }

    /**
     * 转换为持久化数据对象。
     */
    public static LevelRecordDO toDO(LevelRecord value) {
        var row = new LevelRecordDO();
        row.setId(value.id());
        row.setUserId(value.memberId());
        row.setLevelId(value.levelId());
        row.setUserExperience(value.experience());
        row.setExperience(value.recordExperience());
        row.setLevel(value.level());
        row.setDiscountPercent(value.discountPercent());
        row.setDescription(value.description());
        row.setRemark(value.reason());
        row.setCreateTime(value.createTime());
        row.setUpdateTime(value.createTime());
        return row;
    }
}
