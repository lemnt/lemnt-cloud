package com.lemnt.promotion.client.bo;

import java.io.Serializable;
import java.util.List;

/**
 * 活动新报价，只接收身份与SKU数量，不接收客户端活动价。
 */
public record ActivityPriceBo(
    Long memberId,
    String requestNo,
    String activityType,
    Long activityId,
    Long recordId,
    Long headId,
    List<Line> lines)
    implements Serializable {
    public record Line(Long skuId, Integer quantity) implements Serializable {
    }
}
