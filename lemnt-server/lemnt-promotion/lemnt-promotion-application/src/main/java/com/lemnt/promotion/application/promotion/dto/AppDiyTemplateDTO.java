package com.lemnt.promotion.application.promotion.dto;

import java.io.Serializable;

/**
 * 小程序装修模板：首页与我的页面属性。
 */
public record AppDiyTemplateDTO(Long id, String name, String property, String home, String user)
    implements Serializable {
}
