package com.lemnt.mall.application.promotion.dto;

import java.io.Serializable;

/** Banner 应用输出。 */
public record BannerDTO(
        Long id,
        String title,
        String url,
        String picUrl,
        Integer sort,
        Integer status,
        Integer position,
        String memo,
        Integer browseCount
) implements Serializable {
}
