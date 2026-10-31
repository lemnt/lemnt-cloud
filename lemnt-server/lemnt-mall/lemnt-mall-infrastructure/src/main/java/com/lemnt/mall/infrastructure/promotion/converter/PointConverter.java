package com.lemnt.mall.infrastructure.promotion.converter;

import com.lemnt.mall.domain.promotion.model.PointActivity;
import com.lemnt.mall.domain.promotion.model.PointProduct;
import com.lemnt.mall.infrastructure.promotion.dataobject.PointActivityDO;
import com.lemnt.mall.infrastructure.promotion.dataobject.PointProductDO;

/** 积分商城领域对象与表结构转换。 */
public final class PointConverter {
    private PointConverter() {}

    public static PointActivity model(PointActivityDO row) {
        if (row == null) { return null; }
        return new PointActivity(row.getId(), row.getSpuId(), row.getStatus(), row.getRemark(), row.getSort(),
                row.getStock(), row.getTotalStock());
    }

    public static PointActivityDO row(PointActivity value) {
        var row = new PointActivityDO();
        row.setId(value.id());
        row.setSpuId(value.spuId());
        row.setStatus(value.status());
        row.setRemark(value.remark());
        row.setSort(value.sort());
        row.setStock(value.stock());
        row.setTotalStock(value.totalStock());
        return row;
    }

    public static PointProduct model(PointProductDO row) {
        if (row == null) { return null; }
        return new PointProduct(row.getId(), row.getActivityId(), row.getSpuId(), row.getSkuId(),
                row.getExchangeCount(), row.getPoint(), row.getPrice(), row.getStock(),
                row.getActivityStatus());
    }

    public static PointProductDO row(PointProduct value) {
        var row = new PointProductDO();
        row.setId(value.id());
        row.setActivityId(value.activityId());
        row.setSpuId(value.spuId());
        row.setSkuId(value.skuId());
        row.setExchangeCount(value.exchangeCount());
        row.setPoint(value.point());
        row.setPrice(value.price());
        row.setStock(value.stock());
        row.setActivityStatus(value.activityStatus());
        return row;
    }
}
