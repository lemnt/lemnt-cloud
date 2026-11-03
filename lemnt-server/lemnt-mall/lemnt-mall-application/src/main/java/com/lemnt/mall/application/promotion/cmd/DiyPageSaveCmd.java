package com.lemnt.mall.application.promotion.cmd;

import java.io.Serializable;
import java.util.List;

/** 装修页面新增/修改命令，id 为空表示新增。 */
public record DiyPageSaveCmd(
        Long id, Long templateId, String name, String remark, List<String> previewPicUrls)
        implements Serializable {}
