package com.lemnt.trade.infrastructure.aftersale.converter;

import com.lemnt.trade.domain.aftersale.model.AfterSaleLog;
import com.lemnt.trade.infrastructure.aftersale.dataobject.AfterSaleLogDO;

/** 售后日志持久化转换。 */
public final class AfterSaleLogConverter {
    private AfterSaleLogConverter() {}
    /** 转换日志写入行。 */
    public static AfterSaleLogDO toDO(AfterSaleLog value) {
        var row = new AfterSaleLogDO();
        row.setUserId(value.userId());
        row.setUserType(value.userType());
        row.setAfterSaleId(value.afterSaleId());
        row.setBeforeStatus(value.beforeStatus());
        row.setAfterStatus(value.afterStatus());
        row.setOperateType(value.operateType());
        row.setContent(value.content());
        row.setCreateTime(value.createTime());
        return row;
    }
    /** 转换查询结果。 */
    public static AfterSaleLog toModel(AfterSaleLogDO row) {
        return new AfterSaleLog(row.getId(), row.getUserId(), row.getUserType(), row.getAfterSaleId(), row.getBeforeStatus(),
                row.getAfterStatus(), row.getOperateType(), row.getContent(), row.getCreateTime());
    }
}
