package com.lemnt.member.application.account.cmd;

public record PasswordResetCmd(Long memberId, String password) {
    @Override
    public String toString() {
        return "PasswordResetCmd[redacted]";
    }
}
