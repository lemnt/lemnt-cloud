package com.lemnt.member.application.account.cmd;

public record SocialLoginCmd(String provider, String loginCode, String phoneCode, String state) {
    @Override
    public String toString() {
        return "SocialLoginCmd[redacted]";
    }
}
