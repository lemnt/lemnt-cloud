package com.lemnt.member.infrastructure.account.converter;

import com.lemnt.member.domain.account.model.SocialState;
import com.lemnt.member.infrastructure.account.dataobject.SocialStateDO;

public final class SocialStateConverter {
    private SocialStateConverter() {}

    public static SocialState domain(SocialStateDO r) {
        return r == null
                ? null
                : new SocialState(
                        r.getStateHash(),
                        r.getProvider(),
                        r.getPurpose(),
                        r.getMemberId(),
                        r.getExpiresAt());
    }

    public static SocialStateDO row(SocialState v) {
        var r = new SocialStateDO();
        r.setStateHash(v.stateHash());
        r.setProvider(v.provider());
        r.setPurpose(v.purpose());
        r.setMemberId(v.memberId());
        r.setExpiresAt(v.expiresAt());
        return r;
    }
}
