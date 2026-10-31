package com.lemnt.mall.application.coupon.qry;

import java.io.Serializable;

/** 后台持券查询输入。 */
public record CouponManagementPageQry(Long memberId, Long templateId, String status, int pageNum, int pageSize)
        implements Serializable {
}
