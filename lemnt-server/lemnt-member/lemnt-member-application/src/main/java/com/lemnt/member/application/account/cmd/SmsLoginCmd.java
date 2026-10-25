package com.lemnt.member.application.account.cmd;

public record SmsLoginCmd(String mobile, String code) {
    @Override
    public String toString() {
        return "SmsLoginCmd[redacted]";
    }
}
