package com.lemnt.member.adapter.web.user.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

/** 兼容会员入口的基础资料响应。 */
@Schema(description = "会员基础资料")
public record MemberRegisterRes(
        @Schema(description = "会员编号") Long id,
        @Schema(description = "会员业务编号") String memberNo,
        @Schema(description = "昵称") String nickname,
        @Schema(description = "手机号") String mobile,
        @Schema(description = "邮箱") String email,
        @Schema(description = "头像地址") String avatar,
        @Schema(description = "状态") Integer status,
        @Schema(description = "积分") Long points
) implements Serializable {}
