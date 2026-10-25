package com.lemnt.member.infrastructure.account.dataobject;

@lombok.Data
public class CredentialDO {
    private Long memberId;
    private String passwordHash;
}
