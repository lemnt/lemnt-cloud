package com.lemnt.member.infrastructure.user.mapper;

import com.lemnt.member.infrastructure.user.dataobject.MemberDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/** member_user 表数据访问接口。 */
@Mapper
public interface MemberUserMapper extends BaseMapper<MemberDO> {

    /**
     * 获取数据库当前时间。
     */
    @Select("SELECT CURRENT_TIMESTAMP")
    LocalDateTime now();

    /**
     * 根据会员编号读取完整资料。
     */
    default MemberDO profile(Long id) {
        return this.selectOne(Wrappers.<MemberDO>lambdaQuery()
                .eq(MemberDO::getId, id));
    }

    /**
     * 锁定会员资料，串行化同一会员的写操作。
     */
    @Select("SELECT * FROM member_user WHERE id=#{id} FOR UPDATE")
    MemberDO lock(Long id);

    /**
     * 查询会员资料。
     */
    default List<MemberDO> profiles(
            String mobile, int offset, int limit) {
        return this.selectPage(new Page<>((long) offset / limit + 1, limit, false),
                Wrappers.<MemberDO>lambdaQuery()
                .eq(mobile != null, MemberDO::getMobile, mobile)
                .orderByDesc(MemberDO::getId)).getRecords();
    }

    /**
     * 统计符合手机号筛选条件的会员数量。
     */
    default long profileCount(String mobile) {
        return this.selectCount(Wrappers.<MemberDO>lambdaQuery()
                .eq(mobile != null, MemberDO::getMobile, mobile));
    }

    /**
     * 更新会员可维护资料，保留资产与关联数据。
     */
    default int profileUpdate(MemberDO value) {
        return this.update(null, Wrappers.<MemberDO>lambdaUpdate()
                .set(MemberDO::getNickname, value.getNickname())
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
