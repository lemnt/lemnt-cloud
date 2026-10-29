package com.lemnt.mall.application.product.qry;

/** BrowseHistory分页，web可筛选会员，app固定本人。 */
public record ProductBrowseHistoryPageQry(Long userId, Long spuId, Boolean userDeleted, int pageNum, int pageSize) {}
