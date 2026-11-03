package com.lemnt.product.adapter.app.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/** 公开评价独立响应，不输出订单/会员/管理员编号。 */
@Schema(description = "商品评价分页结果")
public record CommentPageRes(
        @Schema(description = "评价编号") Long id,
        @Schema(description = "商品SPU编号") Long spuId,
        @Schema(description = "商品SKU编号") Long skuId,
        @Schema(description = "评价时昵称，匿名显示匿名用户，历史未记录可空") String userNickname,
        @Schema(description = "头像，匿名或历史未记录可空") String userAvatar,
        @Schema(description = "是否匿名，历史未知可空") Boolean anonymous,
        @Schema(description = "综合评分") Integer score,
        @Schema(description = "描述评分，历史未知可空") Integer descriptionScores,
        @Schema(description = "服务评分，历史未知可空") Integer benefitScores,
        @Schema(description = "评价正文") String content,
        @Schema(description = "评价图片，历史未知可空") List<String> picUrls,
        @Schema(description = "原订单商品名称，历史未知可空") String spuName,
        @Schema(description = "原订单图片，历史未知可空") String skuPicUrl,
        @Schema(description = "原订单规格，历史未知可空") List<Spec> skuProperties,
        @Schema(description = "是否回复") boolean replyStatus,
        @Schema(description = "商家回复，未回复可空") String replyContent,
        @Schema(description = "回复时间，可空") LocalDateTime replyTime,
        @Schema(description = "评价时间") LocalDateTime createTime)
        implements java.io.Serializable {
    /** 保存时规格，独立HTTP值对象。 */
    @Schema(description = "商品规格快照")
    public record Spec(
            @Schema(description = "规格编号，历史可空") Long specId,
            @Schema(description = "规格名称") String specName,
            @Schema(description = "规格值编号，历史可空") Long specValueId,
            @Schema(description = "规格值名称") String specValueName)
            implements java.io.Serializable {}
}
