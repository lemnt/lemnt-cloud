package com.lemnt.member.adapter.web.user.converter;

import com.lemnt.member.adapter.web.user.req.GroupAssignReq;
import com.lemnt.member.adapter.web.user.req.MemberCreateReq;
import com.lemnt.member.adapter.web.user.req.MemberGroupCreateReq;
import com.lemnt.member.adapter.web.user.req.MemberGroupUpdateReq;
import com.lemnt.member.adapter.web.user.req.MemberProfileUpdateReq;
import com.lemnt.member.adapter.web.user.req.MemberRegisterReq;
import com.lemnt.member.adapter.web.user.req.MemberTagCreateReq;
import com.lemnt.member.adapter.web.user.req.MemberTagUpdateReq;
import com.lemnt.member.adapter.web.user.req.MemberUserPageReq;
import com.lemnt.member.adapter.web.user.req.PasswordResetReq;
import com.lemnt.member.adapter.web.user.req.TagsAssignReq;
import com.lemnt.member.application.user.cmd.GroupAssignCmd;
import com.lemnt.member.application.user.cmd.GroupSaveCmd;
import com.lemnt.member.application.user.cmd.MemberRegisterCmd;
import com.lemnt.member.application.user.cmd.PasswordResetCmd;
import com.lemnt.member.application.user.cmd.MemberUpdateCmd;
import com.lemnt.member.application.user.cmd.TagSaveCmd;
import com.lemnt.member.application.user.cmd.TagsAssignCmd;
import com.lemnt.member.application.user.qry.MemberPageQry;

/** 会员资料转换器。 */
public final class MemberUserReqConverter {

    /** 转换兼容入口的会员注册请求。 */
    public static MemberRegisterCmd toRegisterCmd(MemberRegisterReq req) {
        return new MemberRegisterCmd(req.nickname(), req.mobile(), req.email(), req.avatar());
    }

    private MemberUserReqConverter() {}

    /**
     * 资料。
     */
    public static MemberUpdateCmd profile(Long id, MemberProfileUpdateReq r) {
        return new MemberUpdateCmd(
                id, r.nickname(), r.email(), r.avatar(), r.sex(), r.birthday(), r.status());
    }

    /**
     * 分组。
     */
    public static GroupAssignCmd group(Long id, GroupAssignReq r) {
        return new GroupAssignCmd(id, r.groupId());
    }

    /**
     * 查询标签。
     */
    public static TagsAssignCmd tags(Long id, TagsAssignReq r) {
        return new TagsAssignCmd(id, r.tagIds());
    }

    /**
     * 更新会员密码凭证。
     */
    public static PasswordResetCmd password(Long id, PasswordResetReq r) {
        return new PasswordResetCmd(id, r.password());
    }

    /**
     * 创建。
     */
    public static MemberRegisterCmd create(MemberCreateReq r) {
        return new MemberRegisterCmd(
                r.nickname(), r.mobile(), r.email(), r.avatar());
    }

    /**
     * 分组。
     */
    public static GroupSaveCmd group(Long id, MemberGroupCreateReq r) {
        return new GroupSaveCmd(id, r.name(), r.remark(), r.status());
    }

    /**
     * 分组。
     */
    public static GroupSaveCmd group(Long id, MemberGroupUpdateReq r) {
        return new GroupSaveCmd(id, r.name(), r.remark(), r.status());
    }

    /**
     * 查询会员资料。
     */
    public static MemberPageQry profiles(MemberUserPageReq r) {
        return new MemberPageQry(r.mobile(), (r.pageNum() == null ? 1 : r.pageNum()), (r.pageSize() == null ? 20 : r.pageSize()));
    }

    /**
     * 标签。
     */
    public static TagSaveCmd tag(Long id, MemberTagCreateReq r) {
        return new TagSaveCmd(id, r.name(), r.remark());
    }

    /**
     * 标签。
     */
    public static TagSaveCmd tag(Long id, MemberTagUpdateReq r) {
        return new TagSaveCmd(id, r.name(), r.remark());
    }
}
