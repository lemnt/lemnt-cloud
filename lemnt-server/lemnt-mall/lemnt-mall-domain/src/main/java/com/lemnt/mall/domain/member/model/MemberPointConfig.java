package com.lemnt.mall.domain.member.model;

/** 会员交易积分配置，unitPrice 为一积分抵扣的整数分，maxPoints 为每单积分上限。 */
public record MemberPointConfig(boolean enabled, int unitPrice, int maxPoints, int givePerYuan) {}
