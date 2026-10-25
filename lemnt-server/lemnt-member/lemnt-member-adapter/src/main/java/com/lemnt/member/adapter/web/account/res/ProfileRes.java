package com.lemnt.member.adapter.web.account.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "会员Profile响应")
public record ProfileRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "记录编号") Long id,
        @io.swagger.v3.oas.annotations.media.Schema(description = "会员业务编号") String memberNo,
        @io.swagger.v3.oas.annotations.media.Schema(description = "昵称") String nickname,
        @io.swagger.v3.oas.annotations.media.Schema(description = "手机号") String mobile,
        @io.swagger.v3.oas.annotations.media.Schema(description = "邮箱") String email,
        @io.swagger.v3.oas.annotations.media.Schema(description = "头像HTTPS地址") String avatar,
        @io.swagger.v3.oas.annotations.media.Schema(description = "状态：1启用，0停用") Integer status,
        @io.swagger.v3.oas.annotations.media.Schema(description = "当前积分") Long points,
        @io.swagger.v3.oas.annotations.media.Schema(description = "经验值或等级升级门槛") Long experience,
        @io.swagger.v3.oas.annotations.media.Schema(description = "等级编号，可空") Long levelId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "分组编号，可空") Long groupId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "性别：0未知，1男，2女") Integer sex,
        @io.swagger.v3.oas.annotations.media.Schema(description = "出生日期")
                java.time.LocalDate birthday,
        @io.swagger.v3.oas.annotations.media.Schema(description = "数据库时间")
                java.time.LocalDateTime createTime)
        implements java.io.Serializable {}
