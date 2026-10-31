package com.lemnt.member.application.user.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.application.user.cmd.MemberUserUpdateCmd;
import com.lemnt.member.domain.shared.model.MemberRules;
import com.lemnt.member.domain.user.gateway.MemberGroupGateway;
import com.lemnt.member.domain.user.gateway.MemberAuthGateway;
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
    private final MemberAuthGateway memberAuthGateway;

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
        return profileUpdate(id, nickname, email, avatar, sex, birthday, status, null);
    }

    /** 后台备注与资料同事务维护；省略保留，空字符串清除。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean profileUpdate(Long id, String nickname, String email, String avatar, Integer sex,
            LocalDate birthday, Integer status, String mark) {
        return profileUpdate(id, nickname, email, avatar, sex, birthday, status, mark, null, null);
    }

    /** 后台维护姓名地区；会员调用省略后保留旧值。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean profileUpdate(Long id, String nickname, String email, String avatar, Integer sex,
            LocalDate birthday, Integer status, String mark, String realName, Integer areaId) {
        return updateProfile(id, nickname, email, avatar, sex, birthday, status, mark, realName, areaId, null);
    }

    /** 后台允许维护手机号，App原命令不进入此入口。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(MemberUserUpdateCmd cmd) {
        updateProfile(cmd.memberId(), cmd.nickname(), cmd.email(), cmd.avatar(), cmd.sex(),
                cmd.birthday(), cmd.status(), cmd.mark(), cmd.name(), cmd.areaId(), cmd.mobile());
        if (Integer.valueOf(0).equals(cmd.status())) {
            // 锁定会员资料期间只要求auth访问缓存，撤销失败则回滚本次保存。
            memberAuthGateway.revokeSessions(cmd.memberId());
        }
        return true;
    }

    /** 锁定原资料后仅替换允许编辑的字段，凭据占用时整次保存失败。 */
    private boolean updateProfile(Long id, String nickname, String email, String avatar, Integer sex,
            LocalDate birthday, Integer status, String mark, String realName, Integer areaId, String mobile) {
        if ((realName != null && realName.length() > 30) || (areaId != null && areaId <= 0))
            throw new ServiceException("姓名或地区不合法", 400);
        if (mark != null && mark.length() > 255) throw new ServiceException("会员备注不能超过255字", 400);
        var old = lock(id);
        String name = MemberRules.text(nickname, 80);
        String mail = MemberRules.optional(email, 254);
        String image = MemberRules.optional(avatar, 500);
        String phone = mobile == null ? old.mobile() : MemberRules.mobile(mobile);
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
        if (phone != null) {
            var owner = memberUserGateway.findByMobile(phone);
            if (owner != null && !id.equals(owner.id())) throw new ServiceException("手机号已被使用", 409);
        }
        if (mail != null) {
            var owner = memberUserGateway.findByEmail(mail);
            if (owner != null && !id.equals(owner.id())) throw new ServiceException("邮箱已被使用", 409);
        }
        memberUserGateway.updateProfile(
                new Member(
                        id,
                        name,
                        phone,
                        mail,
                        image,
                        state,
                        old.points(),
                        old.experience(),
                        old.levelId(),
                        old.groupId(),
                        sex,
                        birthday,
                        old.createTime(), mark == null ? old.mark() : mark,
                        realName == null ? old.name() : realName, areaId == null ? old.areaId() : areaId));
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
        MemberRules.id(id);
        if (memberGroupGateway.group(id) == null) throw new ServiceException("分组不存在", 404);
        if (!memberGroupGateway.groupDelete(id)) throw new ServiceException("分组下有会员，无法删除", 409);
        return true;
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
        MemberRules.id(id);
        if (memberTagGateway.tag(id) == null) throw new ServiceException("标签不存在", 404);
        if (!memberTagGateway.tagDelete(id)) throw new ServiceException("标签下有会员，无法删除", 409);
        return true;
    }
}
