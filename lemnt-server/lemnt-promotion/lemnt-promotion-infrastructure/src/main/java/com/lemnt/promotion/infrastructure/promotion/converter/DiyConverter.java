package com.lemnt.promotion.infrastructure.promotion.converter;

import com.lemnt.promotion.domain.promotion.model.DiyPage;
import com.lemnt.promotion.domain.promotion.model.DiyTemplate;
import com.lemnt.promotion.infrastructure.promotion.dataobject.DiyPageDO;
import com.lemnt.promotion.infrastructure.promotion.dataobject.DiyTemplateDO;

import com.alibaba.fastjson2.JSON;

import java.util.List;

/**
 * 装修领域对象与表结构转换，预览图按 JSON 数组保存。
 */
public final class DiyConverter {
    private DiyConverter() {
    }

    private static List<String> previews(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return JSON.parseArray(value, String.class);
    }

    private static String previews(List<String> values) {
        return JSON.toJSONString(values == null ? List.of() : values);
    }

    public static DiyTemplate model(DiyTemplateDO row) {
        if (row == null) {
            return null;
        }
        return new DiyTemplate(
            row.getId(),
            row.getName(),
            row.getUsed(),
            row.getUsedTime(),
            row.getRemark(),
            previews(row.getPreviewPicUrls()),
            row.getProperty());
    }

    public static DiyTemplateDO row(DiyTemplate value) {
        var row = new DiyTemplateDO();
        row.setId(value.id());
        row.setName(value.name());
        row.setUsed(value.used());
        row.setUsedTime(value.usedTime());
        row.setRemark(value.remark());
        row.setPreviewPicUrls(previews(value.previewPicUrls()));
        row.setProperty(value.property());
        return row;
    }

    public static DiyPage model(DiyPageDO row) {
        if (row == null) {
            return null;
        }
        return new DiyPage(
            row.getId(),
            row.getTemplateId(),
            row.getName(),
            row.getRemark(),
            previews(row.getPreviewPicUrls()),
            row.getProperty());
    }

    public static DiyPageDO row(DiyPage value) {
        var row = new DiyPageDO();
        row.setId(value.id());
        row.setTemplateId(value.templateId());
        row.setName(value.name());
        row.setRemark(value.remark());
        row.setPreviewPicUrls(previews(value.previewPicUrls()));
        row.setProperty(value.property());
        return row;
    }
}
