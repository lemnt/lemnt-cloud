package com.lemnt.mall.application.promotion.qry;

import java.io.Serializable;

/** 装修页面分页查询。 */
public record DiyPagePageQry(Long templateId, String name, int pageNum, int pageSize)
        implements Serializable {
}
