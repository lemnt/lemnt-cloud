package com.lemnt.open.application.index.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

@Schema(description = "首页展示信息")
public record IndexVo(
        @Schema(description = "名称", example = "test") String name
) implements Serializable {
}
