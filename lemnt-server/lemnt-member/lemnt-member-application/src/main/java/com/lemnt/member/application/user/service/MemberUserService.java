package com.lemnt.member.application.user.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.shared.model.MemberRules;
import com.lemnt.member.domain.user.gateway.MemberGroupGateway;
import com.lemnt.member.domain.user.gateway.MemberTagGateway;
import com.lemnt.member.domain.user.gateway.MemberTagLinkGateway;
import com.lemnt.member.domain.user.gateway.MemberUserGateway;
import com.lemnt.member.domain.user.model.Group;
import com.lemnt.member.domain.user.model.Member;
import com.lemnt.member.domain.user.model.Tag;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/** 会员资料与标签分组应用编排。 */
@Component
@RequiredArgsConstructor
public class MemberUserService {

    private final MemberGroupGateway memberGroupGateway;
    private final MemberTagGateway memberTagGateway;
    private final MemberTagLinkGateway memberTagLinkGateway;
    private final MemberUserGateway memberUserGateway;

    private Member lock(Long id) {
        MemberRules.id(id);
        var value = memberUserGateway.lockById(id);
        if (value == null) {
            throw new ServiceException("会员不存在", 400);
        }
        return value;
    }

    /**
     * 更新会员可维护资料，保留资产与关联数据。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean profileUpdate(
            Long id,
            String nickname,
            String email,
            String avatar,
            Integer sex,
            LocalDate birthday,
            Integer status) {
        var old = lock(id);
        String name = MemberRules.text(nickname, 80);
        String mail = MemberRules.optional(email, 254);
        String image = MemberRules.optional(avatar, 500);
        if (mail != null && !mail.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new ServiceException("邮箱不合法", 400);
        }
        if (image != null && !image.startsWith("https://")) {
            throw new ServiceException("头像必须是HTTPS地址", 400);
        }
        if (sex != null && (sex < 0 || sex > 2)) {
            throw new ServiceException("性别不合法", 400);
        }
        if (birthday != null && birthday.isAfter(memberUserGateway.now().toLocalDate())) {
            throw new ServiceException("出生日期不能在未来", 400);
        }
        int state = status == null ? old.status() : status;
        if (state != 0 && state != 1) {
            throw new ServiceException("状态不合法", 400);
        }
        memberUserGateway.updateProfile(
                new Member(
                        id,
                        name,
                        old.mobile(),
                        mail,
                        image,
                        state,
                        old.points(),
                        old.experience(),
                        old.levelId(),
                        old.groupId(),
                        sex,
                        birthday,
                        old.createTime()));
        return true;
    }

    /**
     * 分配分组。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean assignGroup(Long memberId, Long groupId) {
        lock(memberId);
        if (groupId != null) {
            var group = memberGroupGateway.group(MemberRules.id(groupId));
            if (group == null || group.status() != 1) {
                throw new ServiceException("分组不存在或已停用", 400);
            }
        }
        memberUserGateway.assignGroup(memberId, groupId);
        return true;
    }

    /**
     * 校验并替换会员关联标签。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean assignTags(Long memberId, List<Long> tags) {
        lock(memberId);
        if (tags == null || tags.size() > 100 || tags.stream().distinct().count() != tags.size()) {
            throw new ServiceException("标签不合法或重复", 400);
        }
        for (Long id : tags) {
            if (memberTagGateway.tag(MemberRules.id(id)) == null) {
                throw new ServiceException("标签不存在", 400);
            }
        }
        memberTagLinkGateway.memberTags(memberId, tags);
        return true;
    }

    /**
     * 保存分组。
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveGroup(Group v) {
        if (v.id() != null && memberGroupGateway.group(MemberRules.id(v.id())) == null) {
            throw new ServiceException("分组不存在", 400);
        }
        if (v.status() == null || (v.status() != 0 && v.status() != 1)) {
            throw new ServiceException("状态不合法", 400);
        }
        return memberGroupGateway.groupSave(
                new Group(
                        v.id(),
                        MemberRules.text(v.name(), 80),
                        MemberRules.optional(v.remark(), 500),
                        v.status()));
    }

    /**
     * 删除分组。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteGroup(Long id) {
        return memberGroupGateway.groupDelete(MemberRules.id(id));
    }

    /**
     * 保存标签。
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveTag(Tag v) {
        if (v.id() != null && memberTagGateway.tag(MemberRules.id(v.id())) == null) {
            throw new ServiceException("标签不存在", 400);
        }
        return memberTagGateway.tagSave(
                new Tag(
                        v.id(),
                        MemberRules.text(v.name(), 80),
                        MemberRules.optional(v.remark(), 500)));
    }

    /**
     * 删除标签。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTag(Long id) {
        return memberTagGateway.tagDelete(MemberRules.id(id));
    }
}
