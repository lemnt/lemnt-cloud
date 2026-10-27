package com.lemnt.member.domain.user.gateway;

import com.lemnt.member.domain.user.model.Member;

import java.time.LocalDateTime;
import java.util.List;

/** member_user 表领域存储网关。 */
public interface MemberUserGateway {

    /**
     * 获取数据库当前时间。
     */
    LocalDateTime now();

    /**
     * 根据会员编号读取完整资料。
     */
    Member findById(Long id);

    /**
     * 锁定会员资料，串行化同一会员的写操作。
     */
    Member lockById(Long id);

    /**
     * 查询会员资料。
     */
    List<Member> findPage(String mobile, int offset, int limit);

    /**
     * 统计符合手机号筛选条件的会员数量。
     */
    long count(String mobile);

    /**
     * 更新会员可维护资料，保留资产与关联数据。
     */
    void updateProfile(Member value);

    /**
     * 分配分组。
     */
    void assignGroup(Long memberId, Long groupId);

    /** 按手机号查找会员。 */
    Member findByMobile(String mobile);

    /** 按邮箱查找会员。 */
    Member findByEmail(String email);

    /** 保存新会员，返回数据库生成的会员编号与默认资料。 */
    Member save(Member member);

    /**
     * 余额。
     */
    void balance(
            Long memberId,
            String assetType,
            Long balance
    );

    /**
     * 分配等级。
     */
    void assignLevel(Long memberId, Long levelId);

    /** 读取会员密码哈希，仅供密码维护和内部登录资料查询。 */
    String findPasswordByMemberId(Long memberId);

    /** 更新 member_user 的密码字段，不改变其他会员资料。 */
    void updatePassword(Long memberId, String password);
}
