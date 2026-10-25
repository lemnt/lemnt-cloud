package com.lemnt.member.infrastructure.account.converter;

import com.lemnt.member.domain.account.model.SocialBinding;
import com.lemnt.member.infrastructure.account.dataobject.SocialBindingDO;

public final class SocialBindingConverter {
    private SocialBindingConverter() {}

    public static SocialBinding domain(SocialBindingDO r) {
        return r == null
                ? null
                : new SocialBinding(r.getProvider(), r.getSubject(), r.getMemberId());
    }

    public static SocialBindingDO row(SocialBinding v) {
        var r = new SocialBindingDO();
        r.setProvider(v.provider());
        r.setSubject(v.subject());
        r.setMemberId(v.memberId());
        return r;
    }
}
