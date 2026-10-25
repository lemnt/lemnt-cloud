package com.lemnt.member.infrastructure.account.dataobject;

@lombok.Data
public class AddressDO {
    private Long id;
    private Long memberId;
    private String name;
    private String mobile;
    private Long areaId;
    private String detailAddress;
    private Boolean defaultStatus;
}
