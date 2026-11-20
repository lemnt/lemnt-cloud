package com.lemnt.promotion.application.promotion.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 装修模板输出。
 */
public record DiyTemplateDTO(
    Long id,
    String name,
    Boolean used,
    LocalDateTime usedTime,
    String remark,
    List<String> previewPicUrls,
    String property)
    implements Serializable {
    public DiyTemplateDTO {
        previewPicUrls = previewPicUrls == null ? List.of() : List.copyOf(previewPicUrls);
    }
}
