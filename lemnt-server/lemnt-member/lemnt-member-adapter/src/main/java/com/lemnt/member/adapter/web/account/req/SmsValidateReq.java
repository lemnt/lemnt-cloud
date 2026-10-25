package com.lemnt.member.adapter.web.account.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "SmsValidate请求")
public record SmsValidateReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "mobile")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 24)
                String mobile,
        @io.swagger.v3.oas.annotations.media.Schema(description = "scene")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 40)
                String scene,
        @io.swagger.v3.oas.annotations.media.Schema(description = "code")
                @jakarta.validation.constraints.NotBlank
                @jakarta.validation.constraints.Size(max = 256)
                @jakarta.validation.constraints.Pattern(regexp = "[0-9]{6}")
                String code)
        implements java.io.Serializable {
    @Override
    public String toString() {
        return "SmsValidateReq[redacted]";
    }
}
