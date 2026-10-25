package com.lemnt.member.domain.account.model;

public class MemberProviderException extends RuntimeException {
    public MemberProviderException() {
        super("会员外部服务暂不可用");
    }
}
