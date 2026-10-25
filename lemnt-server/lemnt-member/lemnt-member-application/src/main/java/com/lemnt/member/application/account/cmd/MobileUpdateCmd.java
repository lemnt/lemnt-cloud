package com.lemnt.member.application.account.cmd;

public record MobileUpdateCmd(Long memberId, String mobile, String code, String oldCode) {
    @Override
    public String toString() {
        return "MobileUpdateCmd[redacted]";
    }
}
