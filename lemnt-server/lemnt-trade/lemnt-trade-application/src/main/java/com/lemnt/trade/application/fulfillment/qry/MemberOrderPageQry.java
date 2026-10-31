package com.lemnt.trade.application.fulfillment.qry;

import java.io.Serializable;

/** 会员本人订单分页查询。commentStatus 区分待评价与已完成。 */
public record MemberOrderPageQry(
        String status,
        Boolean commentStatus,
        int pageNum,
        int pageSize
) implements Serializable {
}
