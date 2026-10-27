package com.lemnt.member.application.growth.dto;

import java.io.Serializable;

/**
 * 签到配置数据传输对象。
 */
public record SignConfigDTO(
        Integer day,
        Long point,
        Long experience,
        Integer status
) implements Serializable {}
