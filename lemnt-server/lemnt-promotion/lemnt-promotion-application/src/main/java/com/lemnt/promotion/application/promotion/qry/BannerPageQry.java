package com.lemnt.promotion.application.promotion.qry;

import java.io.Serializable;

/** Banner 分页查询。 */
public record BannerPageQry(String title, Integer status, int pageNum, int pageSize)
        implements Serializable {}
