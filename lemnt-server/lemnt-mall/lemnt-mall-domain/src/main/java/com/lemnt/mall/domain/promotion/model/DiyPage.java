package com.lemnt.mall.domain.promotion.model;

import java.io.Serializable;
import java.util.List;

/** 装修页面：属性为 JSON，模板内页面名称唯一。 */
public record DiyPage(
        Long id,
        Long templateId,
        String name,
        String remark,
        List<String> previewPicUrls,
        String property
) implements Serializable {
    public DiyPage {
        previewPicUrls = previewPicUrls == null ? List.of() : List.copyOf(previewPicUrls);
    }
}
