package com.lemnt.member.infrastructure.account.converter;

import com.lemnt.member.domain.account.model.SmsChallenge;
import com.lemnt.member.infrastructure.account.dataobject.SmsChallengeDO;

public final class SmsChallengeConverter {
    private SmsChallengeConverter() {}

    public static SmsChallenge domain(SmsChallengeDO r) {
        return r == null
                ? null
                : new SmsChallenge(
                        r.getMobile(),
                        r.getScene(),
                        r.getCodeHash(),
                        r.getExpiresAt(),
                        r.getAttempts(),
                        r.getLastSentAt(),
                        r.getSentDate(),
                        r.getDailyCount());
    }

    public static SmsChallengeDO row(SmsChallenge v) {
        var r = new SmsChallengeDO();
        r.setMobile(v.mobile());
        r.setScene(v.scene());
        r.setCodeHash(v.codeHash());
        r.setExpiresAt(v.expiresAt());
        r.setAttempts(v.attempts());
        r.setLastSentAt(v.lastSentAt());
        r.setSentDate(v.sentDate());
        r.setDailyCount(v.dailyCount());
        return r;
    }
}
