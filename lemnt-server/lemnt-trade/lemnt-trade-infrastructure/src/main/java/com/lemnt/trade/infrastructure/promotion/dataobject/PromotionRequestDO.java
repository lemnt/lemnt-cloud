package com.lemnt.trade.infrastructure.promotion.dataobject;

import lombok.Data;

@Data
public class PromotionRequestDO {
    private String action;
    private String fingerprint;
    private Long resultId;
    private Long resultRevision;
    private String resultState;
    private Integer resultActiveVersion;
    private Integer resultLastVersion;
}
