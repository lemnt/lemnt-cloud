package com.lemnt.mall.domain.product.model;
/** 由交易真实购买事实确认的旧评价行绑定，不回填商品历史资料。 */
public record CommentOrderLink(Long commentId, Long orderId, Long orderItemId) {}
