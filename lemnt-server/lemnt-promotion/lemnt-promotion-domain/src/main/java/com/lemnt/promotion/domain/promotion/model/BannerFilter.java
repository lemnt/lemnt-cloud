package com.lemnt.promotion.domain.promotion.model;

/** Banner 分页条件。 */
public record BannerFilter(String title, Integer status, int pageNum, int pageSize) {}
