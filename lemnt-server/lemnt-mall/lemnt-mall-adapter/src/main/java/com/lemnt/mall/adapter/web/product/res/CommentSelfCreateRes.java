package com.lemnt.mall.adapter.web.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 后台自评创建结果。 */
@Schema(description = "后台商品自评结果")
public record CommentSelfCreateRes(@Schema(description = "编号") Long id) implements Serializable {}
