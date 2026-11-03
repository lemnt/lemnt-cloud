package com.lemnt.mall.domain.promotion.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/** 装修模板：属性为 JSON，used 表示当前启用模板。 */
public record DiyTemplate(
        Long id,
        String name,
        Boolean used,
        LocalDateTime usedTime,
        String remark,
        List<String> previewPicUrls,
        String property)
        implements Serializable {
    public DiyTemplate {
        previewPicUrls = previewPicUrls == null ? List.of() : List.copyOf(previewPicUrls);
    }
}
