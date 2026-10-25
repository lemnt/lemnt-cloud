package com.lemnt.mall.adapter.web.catalog.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;

@Schema(description = "SpuShelfReq")
public record SpuShelfReq(@Schema(description = "是否上架") @NotNull Boolean onShelf)
        implements Serializable {}
