package com.lemnt.member.infrastructure.user.converter;

import com.lemnt.member.domain.user.model.Member;
import com.lemnt.member.infrastructure.user.dataobject.MemberDO;

/**
 * 资料转换器。
 */
public final class MemberConverter {
    private MemberConverter() {}

    /**
     * 转换为领域对象。
     */
    public static Member toEntity(MemberDO row) {
        return row == null
                ? null
                : new Member(
                        row.getId(),
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

    /**
     * 转换为持久化数据对象。
     */
    public static MemberDO toDO(Member value) {
        var row = new MemberDO();
        row.setId(value.id());

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
