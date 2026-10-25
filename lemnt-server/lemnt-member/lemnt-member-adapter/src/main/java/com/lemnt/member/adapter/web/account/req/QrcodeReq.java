package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "Qrcode请求")
public record QrcodeReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "scene")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 40)
                String scene,
        @io.swagger.v3.oas.annotations.media.Schema(description = "page")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 128)
                String page)
        implements java.io.Serializable {
    @Override
    public String toString() {
        return "QrcodeReq[redacted]";
    }
}
