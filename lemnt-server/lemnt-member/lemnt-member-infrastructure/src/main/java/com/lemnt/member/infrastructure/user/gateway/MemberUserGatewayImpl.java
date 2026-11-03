package com.lemnt.member.infrastructure.user.gateway;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.user.gateway.MemberUserGateway;
import com.lemnt.member.domain.user.model.Member;
import com.lemnt.member.domain.user.model.MemberPageFilter;
import com.lemnt.member.infrastructure.user.converter.MemberConverter;
import com.lemnt.member.infrastructure.user.dataobject.MemberDO;
import com.lemnt.member.infrastructure.user.mapper.MemberUserMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** member_user 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class MemberUserGatewayImpl implements MemberUserGateway {

    private final MemberUserMapper memberUserMapper;

    /**
     * 获取数据库当前时间。
     */
    /** 登录审计不通过资料更新覆盖其他字段。 */
    @Override
    public void recordLogin(Long memberId, java.time.LocalDateTime loginDate, String loginIp) {
        memberUserMapper.update(
                null,
                com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaUpdate(
                                com.lemnt.member.infrastructure.user.dataobject.MemberDO.class)
                        .eq(
                                com.lemnt.member.infrastructure.user.dataobject.MemberDO::getId,
                                memberId)
                        .set(
                                com.lemnt.member.infrastructure.user.dataobject.MemberDO
                                        ::getLoginDate,
                                loginDate)
                        .set(
                                com.lemnt.member.infrastructure.user.dataobject.MemberDO
                                        ::getLoginIp,
                                loginIp));
    }

    @Override
    public LocalDateTime now() {
        return memberUserMapper.now();
    }

    /**
     * 根据会员编号读取完整资料。
     */
    @Override
    public Member findById(Long id) {
        return MemberConverter.toEntity(memberUserMapper.selectById(id));
    }

    /**
     * 锁定会员资料，串行化同一会员的写操作。
     */
    @Override
    public Member lockById(Long id) {
        return MemberConverter.toEntity(memberUserMapper.lock(id));
    }

    /**
     * 查询会员资料。
     */
    @Override
    public List<Member> findPage(MemberPageFilter filter) {
        return memberUserMapper.profiles(filter).stream().map(MemberConverter::toEntity).toList();
    }

    /**
     * 统计符合资料筛选条件的会员数量。
     */
    @Override
    public long count(MemberPageFilter filter) {
        return memberUserMapper.profileCount(filter);
    }

    /**
     * 更新会员可维护资料，保留资产与关联数据。
     */
    @Override
    public void updateProfile(Member value) {
        try {
            profileUpdateRow(MemberConverter.toDO(value));
        } catch (DuplicateKeyException e) {
            // 并发修改以数据库手机号、邮箱唯一约束为准，不暴露SQL异常。
            throw new ServiceException("手机号或邮箱已被使用", 409);
        }
    }

    /**
     * 分配分组。
     */
    @Override
    public void assignGroup(Long memberId, Long groupId) {
        assignGroupRow(memberId, groupId);
    }

    /** 按手机号查找会员。 */
    @Override
    public Member findByMobile(String mobile) {
        return MemberConverter.toEntity(findByMobileRow(mobile));
    }

    /** 按邮箱查找会员。 */
    @Override
    public Member findByEmail(String email) {
        return MemberConverter.toEntity(findByEmailRow(email));
    }

    /** 保存新会员并读取数据库默认值。 */
    @Override
    public Member save(Member member) {
        var row = MemberConverter.toDO(member);
        memberUserMapper.insert(row);
        return findById(row.getId());
    }

    /**
     * 余额。
     */
    @Override
    public void balance(Long memberId, String type, Long balance) {
        if ("POINT".equals(type)) {
            pointsRow(memberId, balance);
        } else if ("EXPERIENCE".equals(type)) {
            experienceRow(memberId, balance);
        } else {
            throw new ServiceException("资产类型不合法", 400);
        }
    }

    /**
     * 分配等级。
     */
    @Override
    public void assignLevel(Long memberId, Long levelId) {
        assignLevelRow(memberId, levelId);
    }

    /** 读取会员表密码，仅在应用层事务中用于登录或改密。 */
    @Override
    public String findPasswordByMemberId(Long memberId) {
        return findPasswordByMemberIdRow(memberId);
    }

    /** 写入由默认密码组件生成的哈希。 */
    @Override
    public void updatePassword(Long memberId, String password) {
        if (updatePasswordRow(memberId, password) != 1) {
            throw new ServiceException("会员不存在", 400);
        }
    }

    /**
     * 更新会员可维护资料，保留资产与关联数据。
     */
    private int profileUpdateRow(MemberDO value) {
        return memberUserMapper.update(
                null,
                Wrappers.<MemberDO>lambdaUpdate()
                        .set(MemberDO::getNickname, value.getNickname())
                        .set(MemberDO::getMobile, value.getMobile())
                        .set(MemberDO::getMark, value.getMark())
                        .set(MemberDO::getName, value.getName())
                        .set(MemberDO::getAreaId, value.getAreaId())
                        .set(MemberDO::getEmail, value.getEmail())
                        .set(MemberDO::getAvatar, value.getAvatar())
                        .set(MemberDO::getStatus, value.getStatus())
                        .set(MemberDO::getSex, value.getSex())
                        .set(MemberDO::getBirthday, value.getBirthday())
                        .eq(MemberDO::getId, value.getId()));
    }

    /**
     * 分配分组。
     */
    private int assignGroupRow(Long memberId, Long groupId) {
        return memberUserMapper.update(
                null,
                Wrappers.<MemberDO>lambdaUpdate()
                        .set(MemberDO::getGroupId, groupId)
                        .eq(MemberDO::getId, memberId));
    }

    /** 按手机号查找会员。 */
    private MemberDO findByMobileRow(String mobile) {
        return memberUserMapper.selectOne(
                Wrappers.<MemberDO>lambdaQuery().eq(MemberDO::getMobile, mobile).last("LIMIT 1"));
    }

    /** 按邮箱查找会员。 */
    private MemberDO findByEmailRow(String email) {
        return memberUserMapper.selectOne(
                Wrappers.<MemberDO>lambdaQuery().eq(MemberDO::getEmail, email).last("LIMIT 1"));
    }

    /**
     * 积分。
     */
    private int pointsRow(Long memberId, Long balance) {
        return memberUserMapper.update(
                null,
                Wrappers.<MemberDO>lambdaUpdate()
                        .set(MemberDO::getPoints, balance)
                        .eq(MemberDO::getId, memberId));
    }

    /**
     * 经验。
     */
    private int experienceRow(Long memberId, Long balance) {
        return memberUserMapper.update(
                null,
                Wrappers.<MemberDO>lambdaUpdate()
                        .set(MemberDO::getExperience, balance)
                        .eq(MemberDO::getId, memberId));
    }

    /**
     * 分配等级。
     */
    private int assignLevelRow(Long memberId, Long levelId) {
        return memberUserMapper.update(
                null,
                Wrappers.<MemberDO>lambdaUpdate()
                        .set(MemberDO::getLevelId, levelId)
                        .eq(MemberDO::getId, memberId));
    }

    /** 读取既有会员表中的密码哈希。 */
    private String findPasswordByMemberIdRow(Long memberId) {
        MemberDO member =
                memberUserMapper.selectOne(
                        Wrappers.<MemberDO>lambdaQuery()
                                .select(MemberDO::getId, MemberDO::getPassword)
                                .eq(MemberDO::getId, memberId));
        return member == null ? null : member.getPassword();
    }

    /** 更新既有会员表中的密码哈希。 */
    private int updatePasswordRow(Long memberId, String password) {
        return memberUserMapper.update(
                null,
                Wrappers.<MemberDO>lambdaUpdate()
                        .set(MemberDO::getPassword, password)
                        .eq(MemberDO::getId, memberId));
    }
}
