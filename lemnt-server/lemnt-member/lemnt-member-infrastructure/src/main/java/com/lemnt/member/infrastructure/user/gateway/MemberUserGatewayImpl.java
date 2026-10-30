package com.lemnt.member.infrastructure.user.gateway;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.member.domain.user.gateway.MemberUserGateway;
import com.lemnt.member.domain.user.model.Member;
import com.lemnt.member.infrastructure.user.converter.MemberConverter;
import com.lemnt.member.infrastructure.user.mapper.MemberUserMapper;

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
        memberUserMapper.update(null, com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaUpdate(com.lemnt.member.infrastructure.user.dataobject.MemberDO.class)
                .eq(com.lemnt.member.infrastructure.user.dataobject.MemberDO::getId, memberId)
                .set(com.lemnt.member.infrastructure.user.dataobject.MemberDO::getLoginDate, loginDate)
                .set(com.lemnt.member.infrastructure.user.dataobject.MemberDO::getLoginIp, loginIp));
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
        return MemberConverter.toEntity(memberUserMapper.profile(id));
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
    public List<Member> findPage(String mobile, int offset, int limit) {
        return memberUserMapper.profiles(mobile, offset, limit).stream()
                .map(MemberConverter::toEntity)
                .toList();
    }

    /**
     * 统计符合手机号筛选条件的会员数量。
     */
    @Override
    public long count(String mobile) {
        return memberUserMapper.profileCount(mobile);
    }

    /**
     * 更新会员可维护资料，保留资产与关联数据。
     */
    @Override
    public void updateProfile(Member value) {
        memberUserMapper.profileUpdate(MemberConverter.toDO(value));
    }

    /**
     * 分配分组。
     */
    @Override
    public void assignGroup(Long memberId, Long groupId) {
        memberUserMapper.assignGroup(memberId, groupId);
    }

    /** 按手机号查找会员。 */
    @Override
    public Member findByMobile(String mobile) {
        return MemberConverter.toEntity(memberUserMapper.findByMobile(mobile));
    }

    /** 按邮箱查找会员。 */
    @Override
    public Member findByEmail(String email) {
        return MemberConverter.toEntity(memberUserMapper.findByEmail(email));
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
            memberUserMapper.points(memberId, balance);
        }
        else if ("EXPERIENCE".equals(type)) {
            memberUserMapper.experience(memberId, balance);
        }
        else {
            throw new ServiceException("资产类型不合法", 400);
        }
    }

    /**
     * 分配等级。
     */
    @Override
    public void assignLevel(Long memberId, Long levelId) {
        memberUserMapper.assignLevel(memberId, levelId);
    }

    /** 读取会员表密码，仅在应用层事务中用于登录或改密。 */
    @Override
    public String findPasswordByMemberId(Long memberId) {
        return memberUserMapper.findPasswordByMemberId(memberId);
    }

    /** 写入由默认密码组件生成的哈希。 */
    @Override
    public void updatePassword(Long memberId, String password) {
        if (memberUserMapper.updatePassword(memberId, password) != 1) {
            throw new ServiceException("会员不存在", 400);
        }
    }
}
