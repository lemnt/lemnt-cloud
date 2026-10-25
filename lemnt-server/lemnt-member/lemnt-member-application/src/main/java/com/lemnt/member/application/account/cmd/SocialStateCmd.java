package com.lemnt.member.application.account.cmd;

public record SocialStateCmd(String provider, String purpose, Long memberId) {
    @Override
    public String toString() {
        return "SocialStateCmd[redacted]";
    }
}
