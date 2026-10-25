package com.lemnt.trade.adapter.web.store.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "CartAdditionRes响应")
public record CartAdditionRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "原请求编号") String requestNo,
        @io.swagger.v3.oas.annotations.media.Schema(description = "SKU编号") Long productId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "本次增加数量") int addedQuantity,
        @io.swagger.v3.oas.annotations.media.Schema(description = "首次成功后的数量快照，不代表当前购物车")
                int resultingQuantity)
        implements java.io.Serializable {}
