package com.lemnt.member.infrastructure.growth.converter;

import com.lemnt.member.domain.growth.model.SignRecord;
import com.lemnt.member.infrastructure.growth.dataobject.SignRecordDO;

/**
 * 签到记录转换器。
 */
public final class SignRecordConverter {
    private SignRecordConverter() {}

    /**
     * 转换为领域对象。
     */
    public static SignRecord toEntity(SignRecordDO row) {
        return row == null
                ? null
                : new SignRecord(
                        row.getId(),
                        row.getUserId(),
                        row.getSignDate(),
                        row.getDay(),
                        row.getPoint(),
                        row.getExperience(), row.getNickname(), row.getCreateTime());
    }

    /**
     * 转换为持久化数据对象。
     */
    public static SignRecordDO toDO(SignRecord value) {
        var row = new SignRecordDO();
        row.setId(value.id());
        row.setUserId(value.memberId());
        row.setSignDate(value.signDate());
        row.setDay(value.day());
        row.setPoint(value.point());
        row.setExperience(value.experience());
        row.setCreateTime(value.createTime());
        row.setUpdateTime(value.createTime());
        return row;
    }
}
