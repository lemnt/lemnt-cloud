package com.lemnt.mall.infrastructure.promotion.converter;

import com.lemnt.mall.domain.promotion.model.SeckillActivity;
import com.lemnt.mall.domain.promotion.model.SeckillConfig;
import com.lemnt.mall.domain.promotion.model.SeckillProduct;
import com.lemnt.mall.infrastructure.promotion.dataobject.SeckillActivityDO;
import com.lemnt.mall.infrastructure.promotion.dataobject.SeckillConfigDO;
import com.lemnt.mall.infrastructure.promotion.dataobject.SeckillProductDO;

import com.alibaba.fastjson2.JSON;

import java.util.Arrays;
import java.util.List;

/** 秒杀领域对象与表结构转换；时段编号按逗号保存以支持 FIND_IN_SET 查询。 */
public final class SeckillConverter {
    private SeckillConverter() {}

    static List<Long> ids(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                .map(Long::valueOf).toList();
    }

    static String ids(List<Long> values) {
        return values == null ? "" : String.join(",", values.stream().map(String::valueOf).toList());
    }

    public static SeckillConfig model(SeckillConfigDO row) {
        if (row == null) { return null; }
        return new SeckillConfig(row.getId(), row.getName(), row.getStartTime(), row.getEndTime(),
                JSON.parseArray(row.getSliderPicUrls(), String.class), row.getStatus());
    }

    public static SeckillConfigDO row(SeckillConfig value) {
        var row = new SeckillConfigDO();
        row.setId(value.id());
        row.setName(value.name());
        row.setStartTime(value.startTime());
        row.setEndTime(value.endTime());
        row.setSliderPicUrls(JSON.toJSONString(value.sliderPicUrls()));
        row.setStatus(value.status());
        return row;
    }

    public static SeckillActivity model(SeckillActivityDO row) {
        if (row == null) { return null; }
        return new SeckillActivity(row.getId(), row.getSpuId(), row.getName(), row.getStatus(), row.getRemark(),
                row.getStartTime(), row.getEndTime(), row.getSort(), ids(row.getConfigIds()),
                row.getTotalLimitCount(), row.getSingleLimitCount(), row.getStock(), row.getTotalStock());
    }

    public static SeckillActivityDO row(SeckillActivity value) {
        var row = new SeckillActivityDO();
        row.setId(value.id());
        row.setSpuId(value.spuId());
        row.setName(value.name());
        row.setStatus(value.status());
        row.setRemark(value.remark());
        row.setStartTime(value.startTime());
        row.setEndTime(value.endTime());
        row.setSort(value.sort());
        row.setConfigIds(ids(value.configIds()));
        row.setTotalLimitCount(value.totalLimitCount());
        row.setSingleLimitCount(value.singleLimitCount());
        row.setStock(value.stock());
        row.setTotalStock(value.totalStock());
        return row;
    }

    public static SeckillProduct model(SeckillProductDO row) {
        if (row == null) { return null; }
        return new SeckillProduct(row.getId(), row.getActivityId(), ids(row.getConfigIds()), row.getSpuId(),
                row.getSkuId(), row.getSeckillPrice(), row.getStock(), row.getActivityStatus(),
                row.getActivityStartTime(), row.getActivityEndTime());
    }

    public static SeckillProductDO row(SeckillProduct value) {
        var row = new SeckillProductDO();
        row.setId(value.id());
        row.setActivityId(value.activityId());
        row.setConfigIds(ids(value.configIds()));
        row.setSpuId(value.spuId());
        row.setSkuId(value.skuId());
        row.setSeckillPrice(value.seckillPrice());
        row.setStock(value.stock());
        row.setActivityStatus(value.activityStatus());
        row.setActivityStartTime(value.activityStartTime());
        row.setActivityEndTime(value.activityEndTime());
        return row;
    }

    public static List<SeckillProduct> products(List<SeckillProductDO> rows) {
        return rows.stream().map(SeckillConverter::model).toList();
    }
}
