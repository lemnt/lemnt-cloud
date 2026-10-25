package com.lemnt.member.application.account.cmd;

public record SocialBindCmd(Long memberId, String provider, String code, String state) {
    @Override
    public String toString() {
        return "SocialBindCmd[redacted]";
    }
}
