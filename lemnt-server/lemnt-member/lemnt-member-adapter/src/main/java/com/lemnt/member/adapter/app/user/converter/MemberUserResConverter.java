package com.lemnt.member.adapter.app.user.converter;

import com.lemnt.member.adapter.app.user.res.ProfileRes;
import com.lemnt.member.application.user.dto.MemberDTO;

/** 会员资料转换器。 */
public final class MemberUserResConverter {

    private MemberUserResConverter() {}

    /**
     * 资料。
     */
    public static ProfileRes profile(MemberDTO v) {
        return v == null
                ? null
                : new ProfileRes(
                        v.id(),
                        v.nickname(),
                        v.mobile(),
                        v.email(),
                        v.avatar(),
                        v.status(),
                        v.points(),
                        v.experience(),
                        v.levelId(),
                        v.groupId(),
                        v.sex(),
                        v.birthday(),
                        v.createTime());
    }

}
