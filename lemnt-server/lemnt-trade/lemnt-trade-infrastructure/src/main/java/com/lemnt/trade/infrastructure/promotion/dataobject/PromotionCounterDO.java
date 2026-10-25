package com.lemnt.trade.infrastructure.promotion.dataobject;

import lombok.Data;

@Data
public class PromotionCounterDO {
    private Long heldCount;
    private Long usedCount;
    private Long globalLimit;
    private Long memberLimit;
}
