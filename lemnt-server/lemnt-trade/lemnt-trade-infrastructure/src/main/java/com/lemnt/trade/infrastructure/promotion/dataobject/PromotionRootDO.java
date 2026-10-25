package com.lemnt.trade.infrastructure.promotion.dataobject;

import lombok.Data;

@Data
public class PromotionRootDO {
    private Long id;
    private String code;
    private String state;
    private Long revision;
    private Integer activeVersion;
    private Integer lastVersion;
    private String draftJson;
}
