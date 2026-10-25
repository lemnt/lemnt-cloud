package com.lemnt.member.domain.account.model;

public record TokenPair(Long memberId, String accessToken, String refreshToken, Long expiresIn) {
    @Override
    public String toString() {
        return "TokenPair[memberId=" + memberId + ",tokens=<redacted>]";
    }
}
