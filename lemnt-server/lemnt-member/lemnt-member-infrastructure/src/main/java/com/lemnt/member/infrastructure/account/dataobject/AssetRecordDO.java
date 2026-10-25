package com.lemnt.member.infrastructure.account.dataobject;

@lombok.Data
public class AssetRecordDO {
    private Long id;
    private Long memberId;
    private String assetType;
    private Long delta;
    private Long balance;
    private String bizType;
    private String bizNo;
    private String reason;
    private java.time.LocalDateTime createTime;
}
