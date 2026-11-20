package com.lemnt.member.adapter.app.user.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * 微信小程序登录请求。
 */
@Schema(description = "WechatLogin请求")
public record WechatLoginReq(
    @Schema(description = "wx.login 返回的一次性登录凭证", example = "081xY3Ga1KkzrS...")
    @NotBlank(message = "微信登录凭证不能为空")
    @Size(max = 128)
    String code)
    implements Serializable {
    @Override
    public String toString() {
        return "WechatLoginReq[redacted]";
    }
}
