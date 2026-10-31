package com.lemnt.trade.adapter.web.aftersale.converter;

import com.lemnt.trade.adapter.web.aftersale.res.AfterSaleLogRes;
import com.lemnt.trade.application.aftersale.dto.AfterSaleLogDTO;

/** 当前售后日志接口转换。 */
public final class AfterSaleLogResConverter {
    private AfterSaleLogResConverter() {}
    /** 转换后台日志响应。 */
    public static AfterSaleLogRes response(AfterSaleLogDTO value) {
        return new AfterSaleLogRes(value.id(), value.userId(), value.userType(), value.afterSaleId(), value.beforeStatus(),
                value.afterStatus(), value.operateType(), value.content(), value.createTime());
    }
}
