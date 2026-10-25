package com.lemnt.member.infrastructure.account.converter;

import com.lemnt.member.domain.account.model.Credential;
import com.lemnt.member.infrastructure.account.dataobject.CredentialDO;

public final class CredentialConverter {
    private CredentialConverter() {}

    public static Credential domain(CredentialDO row) {
        return row == null ? null : new Credential(row.getMemberId(), row.getPasswordHash());
    }

    public static CredentialDO row(Credential value) {
        var row = new CredentialDO();
        row.setMemberId(value.memberId());
        row.setPasswordHash(value.passwordHash());
        return row;
    }
}
