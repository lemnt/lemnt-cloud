package com.lemnt.trade.application.pickup.qry;

import java.io.Serializable;
import java.math.BigDecimal;

/** 小程序自提门店查询，可选会员当前位置用于距离排序。 */
public record AppPickUpStoreListQry(BigDecimal latitude, BigDecimal longitude)
        implements Serializable {
    public AppPickUpStoreListQry {
        if ((latitude == null) != (longitude == null)
                || (latitude != null
                        && (latitude.abs().compareTo(BigDecimal.valueOf(90)) > 0
                                || longitude.abs().compareTo(BigDecimal.valueOf(180)) > 0))) {
            throw new IllegalArgumentException("门店排序坐标不合法");
        }
    }
}
