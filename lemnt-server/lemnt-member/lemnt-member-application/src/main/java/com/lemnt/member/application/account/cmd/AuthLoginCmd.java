package com.lemnt.member.application.account.cmd;

public record AuthLoginCmd(String mobile, String password) {
    @Override
    public String toString() {
        return "AuthLoginCmd[redacted]";
    }
}
