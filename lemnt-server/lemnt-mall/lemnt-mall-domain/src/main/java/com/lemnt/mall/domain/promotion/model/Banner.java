package com.lemnt.mall.domain.promotion.model;

import java.io.Serializable;

/** 营销 Banner：按位置展示，status 0 开启、1 关闭。 */
public record Banner(
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
