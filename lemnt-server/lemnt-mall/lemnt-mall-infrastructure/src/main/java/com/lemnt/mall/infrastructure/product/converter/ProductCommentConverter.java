package com.lemnt.mall.infrastructure.product.converter;

import com.lemnt.mall.domain.product.model.ProductComment;
import com.lemnt.mall.infrastructure.product.dataobject.ProductCommentDO;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import java.util.List;

/** 商品评价持久化转换；JSON只存在基础设施层，历史null不补造。 */
public final class ProductCommentConverter {
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private ProductCommentConverter() {}

    /** 转换领域评价及不可变的评价时快照。 */
    public static ProductComment toModel(ProductCommentDO row) {
        return row == null ? null : new ProductComment(row.getId(), row.getSpuId(), row.getSkuId(), row.getOrderNo(),
                row.getMemberId(), row.getScore(), row.getContent(), row.getVisible(), row.getReplyContent(),
                row.getReplyUserId(), row.getReplyTime(), row.getCreateTime(), row.getDescriptionScores(),
                row.getBenefitScores(), pictures(row.getPicUrls()), row.getAnonymous(), row.getUserNickname(),
                row.getUserAvatar(), row.getOrderId(), row.getSpuName(), row.getSkuPicUrl(),
                properties(row.getSkuProperties()), Boolean.TRUE.equals(row.getReplyStatus()));
    }

    /** 创建不接受客户端设置商家回复、可见性或会员商品快照。 */
    public static ProductCommentDO toDO(ProductComment value) {
        return new ProductCommentDO().setSpuId(value.spuId()).setSkuId(value.skuId()).setOrderNo(value.orderNo())
                .setMemberId(value.memberId()).setScore(value.score()).setContent(value.content()).setVisible(true)
                .setDescriptionScores(value.descriptionScores()).setBenefitScores(value.benefitScores())
                .setPicUrls(value.picUrls() == null ? null : JSON.writeValueAsString(value.picUrls()))
                .setAnonymous(value.anonymous()).setUserNickname(value.userNickname()).setUserAvatar(value.userAvatar())
                .setOrderId(value.orderId()).setSpuName(value.spuName()).setSkuPicUrl(value.skuPicUrl())
                .setSkuProperties(value.skuProperties() == null ? null : JSON.writeValueAsString(value.skuProperties()))
                .setReplyStatus(false);
    }

    private static List<String> pictures(String value) {
        return value == null ? null : JSON.readValue(value, new TypeReference<List<String>>() {});
    }

    private static List<ProductComment.Property> properties(String value) {
        return value == null ? null : JSON.readValue(value, new TypeReference<List<ProductComment.Property>>() {});
    }
}
