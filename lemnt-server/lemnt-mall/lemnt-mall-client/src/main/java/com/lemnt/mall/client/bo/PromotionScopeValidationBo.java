package com.lemnt.mall.client.bo;

import java.io.Serializable;
import java.util.List;

/**
 * 营销范围校验远程参数。
 */
public record PromotionScopeValidationBo(
        String scopeType,
        List<Long> ids
) implements Serializable {}
