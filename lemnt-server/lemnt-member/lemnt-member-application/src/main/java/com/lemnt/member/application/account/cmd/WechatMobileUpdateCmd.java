package com.lemnt.member.application.account.cmd;

public record WechatMobileUpdateCmd(Long memberId, String code) {
    @Override
    public String toString() {
        return "WechatMobileUpdateCmd[redacted]";
    }
}
