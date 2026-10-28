package com.lemnt.mall.infrastructure.promotion.converter;

import com.lemnt.mall.domain.promotion.model.DiscountActivity;
import com.lemnt.mall.domain.promotion.model.DiscountProduct;
import com.lemnt.mall.domain.promotion.model.RewardActivity;
import com.lemnt.mall.domain.promotion.model.RewardRule;
import com.lemnt.mall.infrastructure.promotion.dataobject.DiscountActivityDO;
import com.lemnt.mall.infrastructure.promotion.dataobject.DiscountProductDO;
import com.lemnt.mall.infrastructure.promotion.dataobject.RewardActivityDO;

import com.alibaba.fastjson2.JSON;

/** 活动领域对象与表结构转换。 */
public final class PromotionConverter {
    private PromotionConverter() {}

    public static RewardActivity model(RewardActivityDO row) {
        if (row == null) { return null; }
        return new RewardActivity(
                row.getId(),
                row.getName(),
                row.getStatus(),
                row.getStartTime(),
                row.getEndTime(),
                row.getRemark(),
                row.getConditionType(),
                row.getProductScope(),
                JSON.parseArray(row.getProductScopeValuesJson(), Long.class),
                JSON.parseArray(row.getRulesJson(), RewardRule.class));
    }

    public static RewardActivityDO row(RewardActivity value) {
        var row = new RewardActivityDO();
        row.setId(value.id());
        row.setName(value.name());
        row.setStatus(value.status());
        row.setStartTime(value.startTime());
        row.setEndTime(value.endTime());
        row.setRemark(value.remark());
        row.setConditionType(value.conditionType());
        row.setProductScope(value.productScope());
        row.setProductScopeValuesJson(JSON.toJSONString(value.productScopeValues()));
        row.setRulesJson(JSON.toJSONString(value.rules()));
        return row;
    }

    public static DiscountActivity model(DiscountActivityDO row) {
        if (row == null) { return null; }
        return new DiscountActivity(
                row.getId(),
                row.getName(),
                row.getStatus(),
                row.getStartTime(),
                row.getEndTime(),
                row.getRemark());
    }

    public static DiscountActivityDO row(DiscountActivity value) {
        var row = new DiscountActivityDO();
        row.setId(value.id());
        row.setName(value.name());
        row.setStatus(value.status());
        row.setStartTime(value.startTime());
        row.setEndTime(value.endTime());
        row.setRemark(value.remark());
        return row;
    }

    public static DiscountProduct model(DiscountProductDO row) {
        if (row == null) { return null; }
        return new DiscountProduct(
                row.getId(),
                row.getActivityId(),
                row.getSpuId(),
                row.getSkuId(),
                row.getDiscountType(),
                row.getDiscountPercent(),
                row.getDiscountPrice());
    }

    public static DiscountProductDO row(DiscountProduct value) {
        var row = new DiscountProductDO();
        row.setId(value.id());
        row.setActivityId(value.activityId());
        row.setSpuId(value.spuId());
        row.setSkuId(value.skuId());
        row.setDiscountType(value.discountType());
        row.setDiscountPercent(value.discountPercent());
        row.setDiscountPrice(value.discountPrice());
        return row;
    }
}
