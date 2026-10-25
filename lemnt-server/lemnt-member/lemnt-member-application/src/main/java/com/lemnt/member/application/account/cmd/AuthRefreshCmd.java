package com.lemnt.member.application.account.cmd;

public record AuthRefreshCmd(String refreshToken) {
    @Override
    public String toString() {
        return "AuthRefreshCmd[redacted]";
    }
}
