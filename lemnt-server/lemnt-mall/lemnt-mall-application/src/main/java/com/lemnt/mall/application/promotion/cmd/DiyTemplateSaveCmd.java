package com.lemnt.mall.application.promotion.cmd;

import java.io.Serializable;
import java.util.List;

/** 装修模板新增/修改命令，id 为空表示新增。 */
public record DiyTemplateSaveCmd(Long id, String name, String remark, List<String> previewPicUrls)
        implements Serializable {}
