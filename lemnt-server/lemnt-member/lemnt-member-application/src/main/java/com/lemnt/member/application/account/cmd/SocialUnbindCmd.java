package com.lemnt.member.application.account.cmd;

public record SocialUnbindCmd(Long memberId, String provider) {
    @Override
    public String toString() {
        return "SocialUnbindCmd[redacted]";
    }
}
