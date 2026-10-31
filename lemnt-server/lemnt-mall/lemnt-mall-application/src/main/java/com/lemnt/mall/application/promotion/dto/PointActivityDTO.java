package com.lemnt.mall.application.promotion.dto;

import java.math.BigDecimal;
import java.util.List;

/** 积分商城活动查询结果，展示所需积分最少的商品。 */
public record PointActivityDTO(
        Long id,
        Long spuId,
        String spuName,
        String picUrl,
        Integer status,
        String remark,
        Integer sort,
        Integer stock,
        Integer totalStock,
        Integer point,
        BigDecimal price,
        BigDecimal marketPrice,
        List<PointProductDTO> products
) {}
