package com.lemnt.member.infrastructure.account.converter;

import com.lemnt.member.domain.account.model.Profile;
import com.lemnt.member.infrastructure.account.dataobject.ProfileDO;

public final class ProfileConverter {
    private ProfileConverter() {}

    public static Profile domain(ProfileDO row) {
        return row == null
                ? null
                : new Profile(
                        row.getId(),
                        row.getMemberNo(),
                        row.getNickname(),
                        row.getMobile(),
                        row.getEmail(),
                        row.getAvatar(),
                        row.getStatus(),
                        row.getPoints(),
                        row.getExperience(),
                        row.getLevelId(),
                        row.getGroupId(),
                        row.getSex(),
                        row.getBirthday(),
                        row.getCreateTime());
    }

    public static ProfileDO row(Profile value) {
        var row = new ProfileDO();
        row.setId(value.id());
        row.setMemberNo(value.memberNo());
        row.setNickname(value.nickname());
        row.setMobile(value.mobile());
        row.setEmail(value.email());
        row.setAvatar(value.avatar());
        row.setStatus(value.status());
        row.setPoints(value.points());
        row.setExperience(value.experience());
        row.setLevelId(value.levelId());
        row.setGroupId(value.groupId());
        row.setSex(value.sex());
        row.setBirthday(value.birthday());
        row.setCreateTime(value.createTime());
        return row;
    }
}
