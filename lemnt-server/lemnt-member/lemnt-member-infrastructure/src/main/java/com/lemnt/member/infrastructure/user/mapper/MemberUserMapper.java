package com.lemnt.member.infrastructure.user.mapper;

import com.lemnt.member.infrastructure.user.dataobject.MemberDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.lemnt.member.domain.user.model.MemberPageFilter;

import java.time.LocalDateTime;
import java.util.List;

/** member_user 表数据访问接口。 */
@Mapper
public interface MemberUserMapper extends BaseMapper<MemberDO> {

    /** 真实数据库时间，不由应用时钟推算。 */
    LocalDateTime now();

    /** 锁定会员行，原有写操作序列化不变。 */
    MemberDO lock(@Param("id") Long id);

    /** 参数化标签EXISTS及资料组合分页。 */
    List<MemberDO> profiles(@Param("f") MemberPageFilter filter);

    /** 与资料分页使用同一条件，不重复计算标签命中。 */
    long profileCount(@Param("f") MemberPageFilter filter);

    /**
     * 更新会员可维护资料，保留资产与关联数据。
     */
    default int profileUpdate(MemberDO value) {
        return this.update(null, Wrappers.<MemberDO>lambdaUpdate()
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
    default int assignGroup(Long memberId, Long groupId) {
        return this.update(null, Wrappers.<MemberDO>lambdaUpdate()
                .set(MemberDO::getGroupId, groupId)
                .eq(MemberDO::getId, memberId));
    }

    /** 按手机号查找会员。 */
    default MemberDO findByMobile(String mobile) {
        return this.selectOne(Wrappers.<MemberDO>lambdaQuery()
                .eq(MemberDO::getMobile, mobile)
                .last("LIMIT 1"));
    }

    /** 按邮箱查找会员。 */
    default MemberDO findByEmail(String email) {
        return this.selectOne(Wrappers.<MemberDO>lambdaQuery()
                .eq(MemberDO::getEmail, email)
                .last("LIMIT 1"));
    }

    /**
     * 积分。
     */
    default int points(Long memberId, Long balance) {
        return this.update(null, Wrappers.<MemberDO>lambdaUpdate()
                .set(MemberDO::getPoints, balance)
                .eq(MemberDO::getId, memberId));
    }

    /**
     * 经验。
     */
    default int experience(Long memberId, Long balance) {
        return this.update(null, Wrappers.<MemberDO>lambdaUpdate()
                .set(MemberDO::getExperience, balance)
                .eq(MemberDO::getId, memberId));
    }

    /**
     * 分配等级。
     */
    default int assignLevel(Long memberId, Long levelId) {
        return this.update(null, Wrappers.<MemberDO>lambdaUpdate()
                .set(MemberDO::getLevelId, levelId)
                .eq(MemberDO::getId, memberId));
    }

    /** 读取既有会员表中的密码哈希。 */
    default String findPasswordByMemberId(Long memberId) {
        MemberDO member = this.selectOne(Wrappers.<MemberDO>lambdaQuery()
                .select(MemberDO::getId, MemberDO::getPassword)
                .eq(MemberDO::getId, memberId));
        return member == null ? null : member.getPassword();
    }

    /** 更新既有会员表中的密码哈希。 */
    default int updatePassword(Long memberId, String password) {
        return this.update(null, Wrappers.<MemberDO>lambdaUpdate()
                .set(MemberDO::getPassword, password)
                .eq(MemberDO::getId, memberId));
    }
}
