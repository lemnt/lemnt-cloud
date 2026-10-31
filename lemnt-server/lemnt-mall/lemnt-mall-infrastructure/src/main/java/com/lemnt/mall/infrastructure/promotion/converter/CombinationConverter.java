package com.lemnt.mall.infrastructure.promotion.converter;

import com.lemnt.mall.domain.promotion.model.CombinationActivity;
import com.lemnt.mall.domain.promotion.model.CombinationProduct;
import com.lemnt.mall.domain.promotion.model.CombinationRecord;
import com.lemnt.mall.infrastructure.promotion.dataobject.CombinationActivityDO;
import com.lemnt.mall.infrastructure.promotion.dataobject.CombinationProductDO;
import com.lemnt.mall.infrastructure.promotion.dataobject.CombinationRecordDO;

/** 拼团领域对象与表结构转换。 */
public final class CombinationConverter {
    private CombinationConverter() {}

    public static CombinationActivity model(CombinationActivityDO row) {
        if (row == null) { return null; }
        return new CombinationActivity(row.getId(), row.getName(), row.getSpuId(), row.getTotalLimitCount(),
                row.getSingleLimitCount(), row.getStartTime(), row.getEndTime(), row.getUserSize(),
                row.getVirtualGroup(), row.getStatus(), row.getLimitDuration());
    }

    public static CombinationActivityDO row(CombinationActivity value) {
        var row = new CombinationActivityDO();
        row.setId(value.id());
        row.setName(value.name());
        row.setSpuId(value.spuId());
        row.setTotalLimitCount(value.totalLimitCount());
        row.setSingleLimitCount(value.singleLimitCount());
        row.setStartTime(value.startTime());
        row.setEndTime(value.endTime());
        row.setUserSize(value.userSize());
        row.setVirtualGroup(value.virtualGroup());
        row.setStatus(value.status());
        row.setLimitDuration(value.limitDuration());
        return row;
    }

    public static CombinationProduct model(CombinationProductDO row) {
        if (row == null) { return null; }
        return new CombinationProduct(row.getId(), row.getActivityId(), row.getSpuId(), row.getSkuId(),
                row.getCombinationPrice(), row.getActivityStatus(), row.getActivityStartTime(),
                row.getActivityEndTime());
    }

    public static CombinationProductDO row(CombinationProduct value) {
        var row = new CombinationProductDO();
        row.setId(value.id());
        row.setActivityId(value.activityId());
        row.setSpuId(value.spuId());
        row.setSkuId(value.skuId());
        row.setCombinationPrice(value.combinationPrice());
        row.setActivityStatus(value.activityStatus());
        row.setActivityStartTime(value.activityStartTime());
        row.setActivityEndTime(value.activityEndTime());
        return row;
    }

    public static CombinationRecord model(CombinationRecordDO row) {
        if (row == null) { return null; }
        return new CombinationRecord(row.getId(), row.getActivityId(), row.getCombinationPrice(),
                row.getSpuId(), row.getSpuName(), row.getPicUrl(), row.getSkuId(), row.getCount(),
                row.getUserId(), row.getNickname(), row.getAvatar(), row.getHeadId(), row.getStatus(),
                row.getOrderId(), row.getUserSize(), row.getUserCount(), row.getVirtualGroup(),
                row.getExpireTime(), row.getStartTime(), row.getEndTime());
    }

    public static CombinationRecordDO row(CombinationRecord value) {
        var row = new CombinationRecordDO();
        row.setId(value.id());
        row.setActivityId(value.activityId());
        row.setCombinationPrice(value.combinationPrice());
        row.setSpuId(value.spuId());
        row.setSpuName(value.spuName());
        row.setPicUrl(value.picUrl());
        row.setSkuId(value.skuId());
        row.setCount(value.count());
        row.setUserId(value.userId());
        row.setNickname(value.nickname());
        row.setAvatar(value.avatar());
        row.setHeadId(value.headId());
        row.setStatus(value.status());
        row.setOrderId(value.orderId());
        row.setUserSize(value.userSize());
        row.setUserCount(value.userCount());
        row.setVirtualGroup(value.virtualGroup());
        row.setExpireTime(value.expireTime());
        row.setStartTime(value.startTime());
        row.setEndTime(value.endTime());
        return row;
    }
}
