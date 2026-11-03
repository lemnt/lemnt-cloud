package com.lemnt.promotion.client.dto;

import java.io.Serializable;

/** 营销本地资源处理结果；查询只能返回真实凭证，不默认成功。 */
public record PromotionLockDTO(String orderNo, String status, Long activityRecordId)
        implements Serializable {}
