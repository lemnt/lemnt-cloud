package com.lemnt.member.domain.growth.model;

/**
 * 签到配置。
 */
public record SignConfig(
        Integer day,
        Long point,
        Long experience,
        Integer status
) {}
