package com.lemnt.mall.client.bo;

public record PromotionScopeValidationBo(String scopeType, java.util.List<Long> ids)
        implements java.io.Serializable {}
