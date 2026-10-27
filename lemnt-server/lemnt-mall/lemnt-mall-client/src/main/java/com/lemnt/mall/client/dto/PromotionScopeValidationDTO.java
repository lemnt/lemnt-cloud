package com.lemnt.mall.client.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 营销范围校验数据传输对象。
 */
public record PromotionScopeValidationDTO(
        List<Long> missingIds
) implements Serializable {}
