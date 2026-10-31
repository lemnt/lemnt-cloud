package com.lemnt.mall.domain.promotion.model;

/** 积分商城活动，库存为活动商品库存合计。 */
public record PointActivity(
        Long id,
        Long spuId,
        Integer status,
        String remark,
        Integer sort,
        Integer stock,
        Integer totalStock
) implements java.io.Serializable {
}
