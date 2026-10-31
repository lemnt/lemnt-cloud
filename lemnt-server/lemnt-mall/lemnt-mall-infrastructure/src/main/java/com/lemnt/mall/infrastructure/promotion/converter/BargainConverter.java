package com.lemnt.mall.infrastructure.promotion.converter;

import com.lemnt.mall.domain.promotion.model.BargainActivity;
import com.lemnt.mall.domain.promotion.model.BargainHelp;
import com.lemnt.mall.domain.promotion.model.BargainRecord;
import com.lemnt.mall.infrastructure.promotion.dataobject.BargainActivityDO;
import com.lemnt.mall.infrastructure.promotion.dataobject.BargainHelpDO;
import com.lemnt.mall.infrastructure.promotion.dataobject.BargainRecordDO;

/** 砍价领域对象与表结构转换。 */
public final class BargainConverter {
    private BargainConverter() {}

    public static BargainActivity model(BargainActivityDO row) {
        if (row == null) { return null; }
        return new BargainActivity(row.getId(), row.getName(), row.getStartTime(), row.getEndTime(),
                row.getStatus(), row.getSpuId(), row.getSkuId(), row.getBargainFirstPrice(),
                row.getBargainMinPrice(), row.getStock(), row.getTotalStock(), row.getHelpMaxCount(),
                row.getBargainCount(), row.getTotalLimitCount(), row.getRandomMinPrice(),
                row.getRandomMaxPrice());
    }

    public static BargainActivityDO row(BargainActivity value) {
        var row = new BargainActivityDO();
        row.setId(value.id());
        row.setName(value.name());
        row.setStartTime(value.startTime());
        row.setEndTime(value.endTime());
        row.setStatus(value.status());
        row.setSpuId(value.spuId());
        row.setSkuId(value.skuId());
        row.setBargainFirstPrice(value.bargainFirstPrice());
        row.setBargainMinPrice(value.bargainMinPrice());
        row.setStock(value.stock());
        row.setTotalStock(value.totalStock());
        row.setHelpMaxCount(value.helpMaxCount());
        row.setBargainCount(value.bargainCount());
        row.setTotalLimitCount(value.totalLimitCount());
        row.setRandomMinPrice(value.randomMinPrice());
        row.setRandomMaxPrice(value.randomMaxPrice());
        return row;
    }

    public static BargainRecord model(BargainRecordDO row) {
        if (row == null) { return null; }
        return new BargainRecord(row.getId(), row.getUserId(), row.getActivityId(), row.getSpuId(),
                row.getSkuId(), row.getBargainFirstPrice(), row.getBargainPrice(), row.getStatus(),
                row.getEndTime(), row.getOrderId());
    }

    public static BargainRecordDO row(BargainRecord value) {
        var row = new BargainRecordDO();
        row.setId(value.id());
        row.setUserId(value.userId());
        row.setActivityId(value.activityId());
        row.setSpuId(value.spuId());
        row.setSkuId(value.skuId());
        row.setBargainFirstPrice(value.bargainFirstPrice());
        row.setBargainPrice(value.bargainPrice());
        row.setStatus(value.status());
        row.setEndTime(value.endTime());
        row.setOrderId(value.orderId());
        return row;
    }

    public static BargainHelp model(BargainHelpDO row) {
        if (row == null) { return null; }
        return new BargainHelp(row.getId(), row.getActivityId(), row.getRecordId(), row.getUserId(),
                row.getReducePrice());
    }

    public static BargainHelpDO row(BargainHelp value) {
        var row = new BargainHelpDO();
        row.setId(value.id());
        row.setActivityId(value.activityId());
        row.setRecordId(value.recordId());
        row.setUserId(value.userId());
        row.setReducePrice(value.reducePrice());
        return row;
    }
}
