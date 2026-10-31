package com.lemnt.mall.application.coupon.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.application.coupon.converter.CouponDTOConverter;
import com.lemnt.mall.application.coupon.dto.CouponManagementPageDTO;
import com.lemnt.mall.application.coupon.qry.CouponManagementPageQry;
import com.lemnt.mall.domain.coupon.gateway.CouponGateway;
import com.lemnt.mall.domain.coupon.model.CouponFilter;
import com.lemnt.mall.domain.coupon.model.CouponRules;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/** 后台会员持券分页查询。 */
@Component
@RequiredArgsConstructor
public class CouponManagementPageQueryExe {
    private final CouponGateway coupons;

    /** 限定查询范围和分页大小，返回实际持券快照。 */
    public CouponManagementPageDTO execute(CouponManagementPageQry query) {
        if (query.pageNum() < 1 || query.pageSize() < 1 || query.pageSize() > 100
                || (query.status() != null && !Set.of("AVAILABLE", "LOCKED", "USED", "EXPIRED").contains(query.status()))) {
            throw new ServiceException("持券查询参数不合法", 400);
        }
        if (query.memberId() != null) { CouponRules.id(query.memberId()); }
        if (query.templateId() != null) { CouponRules.id(query.templateId()); }
        var page = coupons.page(new CouponFilter(query.memberId(), query.templateId(), query.status(), query.pageNum(), query.pageSize()));
        return new CouponManagementPageDTO(page.getRecords().stream().map(CouponDTOConverter::coupon).toList(), page.getTotal());
    }
}
