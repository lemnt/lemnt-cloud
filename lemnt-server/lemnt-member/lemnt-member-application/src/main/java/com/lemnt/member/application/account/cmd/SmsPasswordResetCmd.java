package com.lemnt.member.application.account.cmd;

public record SmsPasswordResetCmd(String mobile, String code, String password) {
    @Override
    public String toString() {
        return "SmsPasswordResetCmd[redacted]";
    }
}
