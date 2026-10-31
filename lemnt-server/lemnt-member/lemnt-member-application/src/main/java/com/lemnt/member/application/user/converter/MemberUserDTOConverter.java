package com.lemnt.member.application.user.converter;

import java.util.List;

import com.lemnt.member.application.user.dto.GroupDTO;
import com.lemnt.member.application.user.dto.MemberDTO;
import com.lemnt.member.application.user.dto.TagDTO;
import com.lemnt.member.domain.user.model.Group;
import com.lemnt.member.domain.user.model.Member;
import com.lemnt.member.domain.user.model.Tag;

/** 会员资料转换器。 */
public final class MemberUserDTOConverter {

    private MemberUserDTOConverter() {}

    /**
     * 根据会员编号读取完整资料。
     */
    public static MemberDTO toMemberDTO(Member v) {
        return v == null
                ? null
                : new MemberDTO(
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
                        v.createTime(), v.mark(), v.name(), v.areaId(), v.loginDate(),v.loginIp());
    }

    /**
     * 分组。
     */
    public static GroupDTO toGroupDTO(Group v) {
        return v == null ? null : new GroupDTO(v.id(), v.name(), v.remark(), v.status(), v.createTime());
    }

    /**
     * 标签。
     */
    public static TagDTO toTagDTO(Tag v) {
        return v == null ? null : new TagDTO(v.id(), v.name(), v.remark(), v.createTime());
    }
    /** 基础投影复制后附加当前关联名称，不改原DTO或历史快照。 */
    public static MemberDTO withRelations(MemberDTO value, List<Long> tagIds,
            List<String> tagNames, String levelName, String groupName) {
        return new MemberDTO(value.id(), value.nickname(), value.mobile(), value.email(), value.avatar(), value.status(),
                value.points(), value.experience(), value.levelId(), value.groupId(), value.sex(), value.birthday(),
                value.createTime(), value.mark(), value.name(), value.areaId(), value.loginDate(), value.loginIp(),
                tagIds, tagNames, levelName, groupName);
    }
}
