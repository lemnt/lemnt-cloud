package com.lemnt.mall.application.promotion.qry;

import java.io.Serializable;

/** 装修模板分页查询。 */
public record DiyTemplatePageQry(String name, Boolean used, int pageNum, int pageSize)
        implements Serializable {}
