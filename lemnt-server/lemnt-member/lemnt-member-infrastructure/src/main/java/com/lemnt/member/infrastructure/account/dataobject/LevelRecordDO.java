package com.lemnt.member.infrastructure.account.dataobject;

@lombok.Data
public class LevelRecordDO {
    private Long id;
    private Long memberId;
    private Long levelId;
    private Long experience;
    private String reason;
    private java.time.LocalDateTime createTime;
}
