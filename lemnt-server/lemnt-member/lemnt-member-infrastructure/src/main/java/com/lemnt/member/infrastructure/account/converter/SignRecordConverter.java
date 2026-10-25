package com.lemnt.member.infrastructure.account.converter;

import com.lemnt.member.domain.account.model.SignRecord;
import com.lemnt.member.infrastructure.account.dataobject.SignRecordDO;

public final class SignRecordConverter {
    private SignRecordConverter() {}

    public static SignRecord domain(SignRecordDO row) {
        return row == null
                ? null
                : new SignRecord(
                        row.getId(),
                        row.getMemberId(),
                        row.getSignDate(),
                        row.getSigninDay(),
                        row.getPoint(),
                        row.getExperience());
    }

    public static SignRecordDO row(SignRecord value) {
        var row = new SignRecordDO();
        row.setId(value.id());
        row.setMemberId(value.memberId());
        row.setSignDate(value.signDate());
        row.setSigninDay(value.day());
        row.setPoint(value.point());
        row.setExperience(value.experience());
        return row;
    }
}
