package com.lemnt.member.infrastructure.account.dataobject;

@lombok.Data
public class SocialBindingDO {
    private String provider;
    private String subject;
    private Long memberId;
}
