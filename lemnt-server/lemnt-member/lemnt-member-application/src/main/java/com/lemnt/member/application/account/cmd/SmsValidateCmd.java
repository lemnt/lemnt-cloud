package com.lemnt.member.application.account.cmd;

public record SmsValidateCmd(Long memberId, String mobile, String scene, String code) {
    @Override
    public String toString() {
        return "SmsValidateCmd[redacted]";
    }
}
