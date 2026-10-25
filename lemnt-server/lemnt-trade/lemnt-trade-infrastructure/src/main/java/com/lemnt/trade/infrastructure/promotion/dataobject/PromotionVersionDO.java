package com.lemnt.trade.infrastructure.promotion.dataobject;

import lombok.Data;

@Data
public class PromotionVersionDO {
    private Long rootId;
    private Integer version;
    private String draftJson;
    private String ruleHash;
    private String algorithmVersion;
    private Long startsAtEpochMs;
    private Long endsAtEpochMs;
    private Long operatorId;
    private Long publishedAtEpochMs;
}
