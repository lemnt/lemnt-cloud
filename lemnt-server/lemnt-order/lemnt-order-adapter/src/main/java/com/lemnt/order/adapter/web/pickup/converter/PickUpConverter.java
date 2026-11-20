package com.lemnt.order.adapter.web.pickup.converter;

import com.lemnt.order.adapter.web.pickup.req.PickUpStorePageReq;
import com.lemnt.order.adapter.web.pickup.req.PickUpStoreSaveReq;
import com.lemnt.order.adapter.web.pickup.res.PickUpOrderRes;
import com.lemnt.order.adapter.web.pickup.res.PickUpStoreRes;
import com.lemnt.order.application.pickup.cmd.PickUpStoreSaveCmd;
import com.lemnt.order.application.pickup.dto.PickUpOrderDTO;
import com.lemnt.order.application.pickup.dto.PickUpStoreDTO;
import com.lemnt.order.application.pickup.qry.PickUpStorePageQry;

/**
 * 自提门店请求及响应转换。
 */
public final class PickUpConverter {
    private PickUpConverter() {
    }

    public static PickUpStoreSaveCmd store(PickUpStoreSaveReq req) {
        return new PickUpStoreSaveCmd(
            null,
            req.name(),
            req.introduction(),
            req.phone(),
            req.areaId(),
            req.detailAddress(),
            req.logo(),
            req.openingTime(),
            req.closingTime(),
            req.latitude(),
            req.longitude(),
            req.verifyUserIds(),
            req.status());
    }

    public static PickUpStoreSaveCmd store(Long id, PickUpStoreSaveReq req) {
        return new PickUpStoreSaveCmd(
            id,
            req.name(),
            req.introduction(),
            req.phone(),
            req.areaId(),
            req.detailAddress(),
            req.logo(),
            req.openingTime(),
            req.closingTime(),
            req.latitude(),
            req.longitude(),
            req.verifyUserIds(),
            req.status());
    }

    public static PickUpStorePageQry page(PickUpStorePageReq req) {
        return new PickUpStorePageQry(
            req.name(),
            req.status(),
            req.pageNum() == null ? 1 : req.pageNum(),
            req.pageSize() == null ? 20 : req.pageSize());
    }

    public static PickUpStoreRes store(PickUpStoreDTO value) {
        return new PickUpStoreRes(
            value.id(),
            value.name(),
            value.introduction(),
            value.phone(),
            value.areaId(),
            value.detailAddress(),
            value.logo(),
            value.openingTime(),
            value.closingTime(),
            value.latitude(),
            value.longitude(),
            value.verifyUserIds(),
            value.status());
    }

    public static PickUpOrderRes order(PickUpOrderDTO value) {
        return new PickUpOrderRes(
            value.orderNo(),
            value.status(),
            value.storeId(),
            value.storeName(),
            value.storePhone(),
            value.storeAddress(),
            value.verifyCode());
    }
}
