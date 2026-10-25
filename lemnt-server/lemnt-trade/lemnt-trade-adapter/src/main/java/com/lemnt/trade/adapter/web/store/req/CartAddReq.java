package com.lemnt.trade.adapter.web.store.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "CartAddReq请求")
public record CartAddReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "本次增加数量1至999")
                @jakarta.validation.constraints.Min(1)
                @jakarta.validation.constraints.Max(999)
                int quantity,
        @io.swagger.v3.oas.annotations.media.Schema(description = "会员范围内幂等请求编号")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 64)
                String requestNo)
        implements java.io.Serializable {}
