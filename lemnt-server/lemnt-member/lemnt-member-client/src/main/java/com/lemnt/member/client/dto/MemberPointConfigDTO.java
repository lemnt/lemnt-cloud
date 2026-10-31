package com.lemnt.member.client.dto;

/** 交易积分配置，单积分抵扣金额单位分，maxPoints 为每单最多抵扣积分数。 */
public record MemberPointConfigDTO(boolean enabled, int unitPrice, int maxPoints, int givePerYuan)
        implements java.io.Serializable {}
