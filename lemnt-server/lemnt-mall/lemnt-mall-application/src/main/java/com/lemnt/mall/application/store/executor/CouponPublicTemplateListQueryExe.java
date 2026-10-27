package com.lemnt.mall.application.store.executor;

import com.lemnt.mall.application.store.service.StorefrontService;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 营销查询执行器。
 */
@Component
@RequiredArgsConstructor
public class CouponPublicTemplateListQueryExe {
    private final StorefrontService storefrontService;

    /**
     * 执行营销查询。
     */
    public List<Long> execute() {
        return storefrontService.publicIds();
    }
}
