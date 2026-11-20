package com.lemnt.promotion.application.promotion.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 装修页面输出。
 */
public record DiyPageDTO(
    Long id,
    Long templateId,
    String name,
    String remark,
    List<String> previewPicUrls,
    String property)
    implements Serializable {
    public DiyPageDTO {
        previewPicUrls = previewPicUrls == null ? List.of() : List.copyOf(previewPicUrls);
    }
}
