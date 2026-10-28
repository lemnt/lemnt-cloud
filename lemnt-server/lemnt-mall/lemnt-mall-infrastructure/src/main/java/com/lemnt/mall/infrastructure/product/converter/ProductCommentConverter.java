package com.lemnt.mall.infrastructure.product.converter;

import com.lemnt.mall.domain.product.model.ProductComment;
import com.lemnt.mall.infrastructure.product.dataobject.ProductCommentDO;

/** 商品评价持久化转换。 */
public final class ProductCommentConverter {
    private ProductCommentConverter() {
    }

    /** 转换领域评价。 */
    public static ProductComment toModel(ProductCommentDO row) {
        return row == null ? null : new ProductComment(row.getId(), row.getSpuId(), row.getSkuId(), row.getOrderNo(),
                row.getMemberId(), row.getScore(), row.getContent(), row.getVisible(), row.getReplyContent(),
                row.getReplyUserId(), row.getReplyTime(), row.getCreateTime());
    }

    /** 转换新增评价字段，回复只能由商家回复接口写入。 */
    public static ProductCommentDO toDO(ProductComment value) {
        return new ProductCommentDO().setSpuId(value.spuId()).setSkuId(value.skuId()).setOrderNo(value.orderNo())
                .setMemberId(value.memberId()).setScore(value.score()).setContent(value.content()).setVisible(true);
    }
}
