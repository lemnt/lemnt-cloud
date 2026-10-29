package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 商品评价分页响应。 */
@Schema(description = "商品评价分页响应")
public record CommentPageRes(
        @Schema(description = "评价ID")
        Long id,
        @Schema(description = "SPU编号")
        Long spuId,
        @Schema(description = "SKU编号")
        Long skuId,
        @Schema(description = "原订单编号")
        String orderNo,
        @Schema(description = "真实评价会员ID")
        Long memberId,
        @Schema(description = "评分，1至5")
        Integer score,
        @Schema(description = "评价内容")
        String content,
        @Schema(description = "是否显示")
        boolean visible,
        @Schema(description = "商家回复，可空")
        String replyContent,
        @Schema(description = "回复管理员ID，可空")
        Long replyUserId,
        @Schema(description = "回复时间，可空")
        LocalDateTime replyTime,
        @Schema(description = "评价时间")
        LocalDateTime createTime,
        @Schema(description = "描述评分，历史未知可空") Integer descriptionScores,
        @Schema(description = "服务评分，历史未知可空") Integer benefitScores,
        @Schema(description = "评价图片，历史未知可空") java.util.List<String> picUrls,
        @Schema(description = "是否匿名，历史未知可空") Boolean anonymous,
        @Schema(description = "评价时会员昵称，可空") String userNickname,
        @Schema(description = "评价时头像，可空") String userAvatar,
        @Schema(description = "原订单数据库编号，可空") Long orderId,
        @Schema(description = "原订单商品名称，可空") String spuName,
        @Schema(description = "原订单商品图片，可空") String skuPicUrl,
        @Schema(description = "原订单商品规格快照，可空") java.util.List<Property> skuProperties,
        @Schema(description = "是否已商家回复") boolean replyStatus
) implements Serializable {
    /** 保存时的规格编号及名称。 */
    public record Property(
            @Schema(description = "规格编号，历史可空") Long propertyId,
            @Schema(description = "规格名称") String propertyName,
            @Schema(description = "规格值编号，历史可空") Long valueId,
            @Schema(description = "规格值名称") String valueName
    ) implements Serializable {}
}
