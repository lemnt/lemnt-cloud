package com.lemnt.member.adapter.app.user.converter;

import com.lemnt.member.adapter.app.user.req.PasswordChangeReq;
import com.lemnt.member.adapter.app.user.req.ProfileUpdateReq;
import com.lemnt.member.application.user.cmd.PasswordChangeCmd;
import com.lemnt.member.application.user.cmd.MemberUpdateCmd;

/** 会员资料转换器。 */
public final class MemberUserReqConverter {

    private MemberUserReqConverter() {}

    /**
     * 资料。
     */
    public static MemberUpdateCmd profile(Long id, ProfileUpdateReq r) {
        return new MemberUpdateCmd(
                id, r.nickname(), r.email(), r.avatar(), r.sex(), r.birthday(), null);
    }

    /**
     * 更新会员密码凭证。
     */
    public static PasswordChangeCmd password(Long id, PasswordChangeReq r) {
        return new PasswordChangeCmd(id, r.oldPassword(), r.newPassword());
    }

}
