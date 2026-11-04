package com.lemnt.promotion.domain.promotion.model;

import java.io.Serializable;
import java.util.List;

/**
 * 秒杀时段，使用 HH:mm:ss 字符串保存每日时间点。
 */
public record SeckillConfig(
    Long id,
    String name,
    String startTime,
    String endTime,
    List<String> sliderPicUrls,
    Integer status)
    implements Serializable {
    public SeckillConfig {
        sliderPicUrls = sliderPicUrls == null ? List.of() : List.copyOf(sliderPicUrls);
    }
}
