package com.lemnt.member.infrastructure.account.converter;

import com.lemnt.member.domain.account.model.Session;
import com.lemnt.member.infrastructure.account.dataobject.SessionDO;

public final class SessionConverter {
    private SessionConverter() {}

    public static Session domain(SessionDO row) {
        return row == null
                ? null
                : new Session(
                        row.getId(),
                        row.getMemberId(),
                        row.getAccessHash(),
                        row.getRefreshHash(),
                        row.getExpiresAt(),
                        row.getRefreshExpiresAt());
    }

    public static SessionDO row(Session value) {
        var row = new SessionDO();
        row.setId(value.id());
        row.setMemberId(value.memberId());
        row.setAccessHash(value.accessHash());
        row.setRefreshHash(value.refreshHash());
        row.setExpiresAt(value.expiresAt());
        row.setRefreshExpiresAt(value.refreshExpiresAt());
        return row;
    }
}
