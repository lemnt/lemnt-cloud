package com.lemnt.member.application.account.cmd;

public record PasswordChangeCmd(Long memberId, String oldPassword, String newPassword) {
    @Override
    public String toString() {
        return "PasswordChangeCmd[redacted]";
    }
}
