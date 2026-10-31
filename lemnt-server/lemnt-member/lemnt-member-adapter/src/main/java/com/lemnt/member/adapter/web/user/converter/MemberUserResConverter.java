package com.lemnt.member.adapter.web.user.converter;

import com.lemnt.member.adapter.web.user.res.MemberCatalogQueryGroupsPageRes;
import com.lemnt.member.adapter.web.user.res.MemberCatalogQueryTagsPageRes;
import com.lemnt.member.adapter.web.user.res.MemberGroupDetailRes;
import com.lemnt.member.adapter.web.user.res.MemberGroupListRes;
import com.lemnt.member.adapter.web.user.res.MemberGroupSimpleRes;
import com.lemnt.member.adapter.web.user.res.MemberTagDetailRes;
import com.lemnt.member.adapter.web.user.res.MemberTagListRes;
import com.lemnt.member.adapter.web.user.res.MemberTagSimpleRes;
import com.lemnt.member.adapter.web.user.res.MemberUserDetailRes;
import com.lemnt.member.adapter.web.user.res.MemberUserPageRes;
import com.lemnt.member.application.user.dto.GroupDTO;
import com.lemnt.member.application.user.dto.MemberDTO;
import com.lemnt.member.application.user.dto.TagDTO;

/** 会员资料转换器。 */
public final class MemberUserResConverter {

    private MemberUserResConverter() {}

    /**
     * 分组。
     */
    public static MemberCatalogQueryGroupsPageRes toMemberCatalogQueryGroupsPageRes(GroupDTO v) {
        return v == null ? null : new MemberCatalogQueryGroupsPageRes(v.id(), v.name(), v.remark(), v.status(), v.createTime());
    }

    /**
     * 分组。
     */
    public static MemberGroupListRes toMemberGroupListRes(GroupDTO v) {
        return v == null ? null : new MemberGroupListRes(v.id(), v.name(), v.remark(), v.status(), v.createTime());
    }

    /**
     * 分组。
     */
    public static MemberGroupDetailRes toMemberGroupDetailRes(GroupDTO v) {
        return v == null ? null : new MemberGroupDetailRes(v.id(), v.name(), v.remark(), v.status(), v.createTime());
    }

    /**
     * 分组。
     */
    public static MemberGroupSimpleRes toMemberGroupSimpleRes(GroupDTO v) {
        return v == null ? null : new MemberGroupSimpleRes(v.id(), v.name(), v.remark(), v.status());
    }

    /**
     * 标签。
     */
    public static MemberCatalogQueryTagsPageRes toMemberCatalogQueryTagsPageRes(TagDTO v) {
        return v == null ? null : new MemberCatalogQueryTagsPageRes(v.id(), v.name(), v.remark(), v.createTime());
    }

    /**
     * 标签。
     */
    public static MemberTagListRes toMemberTagListRes(TagDTO v) {
        return v == null ? null : new MemberTagListRes(v.id(), v.name(), v.remark(), v.createTime());
    }

    /**
     * 标签。
     */
    public static MemberTagDetailRes toMemberTagDetailRes(TagDTO v) {
        return v == null ? null : new MemberTagDetailRes(v.id(), v.name(), v.remark(), v.createTime());
    }

    /**
     * 标签。
     */
    public static MemberTagSimpleRes toMemberTagSimpleRes(TagDTO v) {
        return v == null ? null : new MemberTagSimpleRes(v.id(), v.name(), v.remark());
    }


    /**
     * 资料。
     */
    public static MemberUserPageRes toMemberUserPageRes(MemberDTO v) {
        return v == null
                ? null
                : new MemberUserPageRes(
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
                        v.createTime(), v.mark(), v.name(), v.areaId(), v.loginDate(),v.loginIp(),
                        v.tagIds(), v.tagNames(), v.levelName(), v.groupName());
    }

    /**
     * 资料。
     */
    public static MemberUserDetailRes toMemberUserDetailRes(MemberDTO v) {
        return v == null
                ? null
                : new MemberUserDetailRes(
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
                        v.createTime(), v.mark(), v.name(), v.areaId(), v.loginDate(),v.loginIp(),
                        v.tagIds(), v.tagNames(), v.levelName(), v.groupName());
    }


}
