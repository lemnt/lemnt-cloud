package com.lemnt.promotion.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.List;

/**
 * 装修模板新增/修改请求。
 */
@Schema(description = "装修模板新增/修改请求")
public record DiyTemplateSaveReq(
    @Schema(description = "模板名称") @NotBlank @Size(max = 100) String name,
    @Schema(description = "备注") @Size(max = 255) String remark,
    @Schema(description = "预览图") List<String> previewPicUrls)
    implements Serializable {
}
