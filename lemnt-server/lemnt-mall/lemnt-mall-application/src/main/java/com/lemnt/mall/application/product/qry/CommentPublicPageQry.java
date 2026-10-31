package com.lemnt.mall.application.product.qry;
/** 商品公开评价分页，只接受SPU及评分分组。 */
public record CommentPublicPageQry(Long spuId, int type, int pageNum, int pageSize) {}
