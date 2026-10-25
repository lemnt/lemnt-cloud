package com.lemnt.member.application.account.cmd;

public record SmsSendCmd(Long memberId, String mobile, String scene) {
    @Override
    public String toString() {
        return "SmsSendCmd[redacted]";
    }
}
