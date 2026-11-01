package com.lemnt.mall.application.promotion.dto;

import java.io.Serializable;

/** 小程序 Banner 输出，不暴露排序、状态和统计字段。 */
public record AppBannerDTO(Long id, String title, String url, String picUrl) implements Serializable {
}
