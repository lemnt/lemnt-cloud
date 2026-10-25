package com.lemnt.member.adapter.web.account.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "会员Address响应")
public record AddressRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "记录编号") Long id,
        @io.swagger.v3.oas.annotations.media.Schema(description = "会员编号") Long memberId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "名称或收件人") String name,
        @io.swagger.v3.oas.annotations.media.Schema(description = "手机号") String mobile,
        @io.swagger.v3.oas.annotations.media.Schema(description = "地区编号") Long areaId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "详细地址") String detailAddress,
        @io.swagger.v3.oas.annotations.media.Schema(description = "是否默认地址") Boolean defaultStatus)
        implements java.io.Serializable {}
