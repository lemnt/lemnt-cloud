package com.lemnt.promotion.infrastructure.promotion.converter;

import com.lemnt.promotion.domain.promotion.model.Banner;
import com.lemnt.promotion.infrastructure.promotion.dataobject.BannerDO;

/**
 * Banner 领域对象与表结构转换。
 */
public final class BannerConverter {
    private BannerConverter() {
    }

    public static Banner model(BannerDO row) {
        if (row == null) {
            return null;
        }
        return new Banner(
            row.getId(),
            row.getTitle(),
            row.getUrl(),
            row.getPicUrl(),
            row.getSort(),
            row.getStatus(),
            row.getPosition(),
            row.getMemo(),
            row.getBrowseCount());
    }

    public static BannerDO row(Banner value) {
        var row = new BannerDO();
        row.setId(value.id());
        row.setTitle(value.title());
        row.setUrl(value.url());
        row.setPicUrl(value.picUrl());
        row.setSort(value.sort());
        row.setStatus(value.status());
        row.setPosition(value.position());
        row.setMemo(value.memo());
        row.setBrowseCount(value.browseCount());
        return row;
    }
}
