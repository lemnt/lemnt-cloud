package com.lemnt.member.infrastructure.account.dataobject;

@lombok.Data
public class SignRecordDO {
    private Long id;
    private Long memberId;
    private java.time.LocalDate signDate;
    private Integer signinDay;
    private Long point;
    private Long experience;
}
