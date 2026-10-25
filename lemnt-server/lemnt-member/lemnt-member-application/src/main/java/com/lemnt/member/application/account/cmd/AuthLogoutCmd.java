package com.lemnt.member.application.account.cmd;

public record AuthLogoutCmd(String authorization) {
    @Override
    public String toString() {
        return "AuthLogoutCmd[redacted]";
    }
}
