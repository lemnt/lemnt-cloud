package com.lemnt.product.adapter.app.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 门店详情响应。
 */
@Schema(description = "StoreDetail公开响应")
public record StoreDetailRes(
        @Schema(description = "id") Long id,
        @Schema(description = "name") String name,
        @Schema(description = "categoryId") Long categoryId,
        @Schema(description = "brandId") Long brandId,
        @Schema(description = "description") String description,
        @Schema(description = "image") String image,
        @Schema(description = "skus") List<StoreSkuRes> skus,
        @Schema(description = "已售件数，支付确认累计") Integer salesCount,
        @Schema(description = "商品简介，历史未知为null") String introduction,
        @Schema(description = "轮播图片，历史未知为null") List<String> sliderPicUrls,
        @Schema(description = "配送方式：1快递、2自提，未配置为空列表") List<Integer> deliveryTypes,
        @Schema(description = "配送模板编号，未配置为null") Long deliveryTemplateId,
        @Schema(description = "展示销量为实销量加虚拟量，虚拟量未知时为null") Long displaySalesCount)
        implements Serializable {}
