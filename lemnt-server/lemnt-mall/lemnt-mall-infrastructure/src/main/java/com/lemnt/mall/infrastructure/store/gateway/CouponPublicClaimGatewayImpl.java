package com.lemnt.mall.infrastructure.store.gateway;

import com.lemnt.mall.domain.store.gateway.CouponPublicClaimGateway;
import com.lemnt.mall.infrastructure.store.mapper.CouponPublicClaimMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** CouponPublicClaim 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class CouponPublicClaimGatewayImpl implements CouponPublicClaimGateway {
    private final CouponPublicClaimMapper couponPublicClaimMapper;

    @Override
    /**
     * 可领取。
     */
    public boolean claimable(Long id) {
        return Boolean.TRUE.equals(couponPublicClaimMapper.claimable(id));
    }

    @Override
    /**
     * 可领取。
     */
    public void claimable(Long id, boolean enabled) {
        couponPublicClaimMapper.setClaimable(id, enabled);
    }

    @Override
    /**
     * 公开模板编号集合。
     */
    public List<Long> publicTemplateIds() {
        var rows = couponPublicClaimMapper.publicTemplateIds();
        if (rows.size() > 1000) {
            throw new IllegalStateException("公开模板超过1000，请接入分页");
        }
        return rows;
    }
}
