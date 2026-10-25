package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "Jsapi请求")
public record JsapiReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "url")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 2048)
                String url)
        implements java.io.Serializable {
    @Override
    public String toString() {
        return "JsapiReq[redacted]";
    }
}
