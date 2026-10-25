package com.lemnt.member.infrastructure.account.converter;

import com.lemnt.member.domain.account.model.SignConfig;
import com.lemnt.member.infrastructure.account.dataobject.SignConfigDO;

public final class SignConfigConverter {
    private SignConfigConverter() {}

    public static SignConfig domain(SignConfigDO row) {
        return row == null
                ? null
                : new SignConfig(
                        row.getSigninDay(), row.getPoint(), row.getExperience(), row.getStatus());
    }

    public static SignConfigDO row(SignConfig value) {
        var row = new SignConfigDO();
        row.setSigninDay(value.day());
        row.setPoint(value.point());
        row.setExperience(value.experience());
        row.setStatus(value.status());
        return row;
    }
}
