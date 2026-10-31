package com.lemnt.member.domain.growth.model;

/** 签到配置，ID用于定位，天数是可调整的唯一业务属性。 */
public record SignConfig(
        Long id,
        Integer day,
        Long point,
        Long experience,
        Integer status
) {
    /** 创建尚未分配ID的规则。 */
    public SignConfig(Integer day, Long point, Long experience, Integer status) {
        this(null, day, point, experience, status);
    }
}
