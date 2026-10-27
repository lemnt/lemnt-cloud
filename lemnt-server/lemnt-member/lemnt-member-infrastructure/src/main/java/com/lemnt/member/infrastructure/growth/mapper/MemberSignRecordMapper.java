package com.lemnt.member.infrastructure.growth.mapper;

import com.lemnt.member.domain.growth.model.SignRecordPageFilter;
import com.lemnt.member.infrastructure.growth.dataobject.SignRecordDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/** member_sign_record 表数据访问接口。 */
@Mapper
public interface MemberSignRecordMapper extends BaseMapper<SignRecordDO> {

    /**
     * 签到日期。
     */
    default SignRecordDO signDate(Long memberId, LocalDate date) {
        return this.selectOne(Wrappers.<SignRecordDO>lambdaQuery()
                .eq(SignRecordDO::getMemberId, memberId)
                .eq(SignRecordDO::getSignDate, date));
    }

    /**
     * 最近一次签到。
     */
    default SignRecordDO lastSign(Long memberId) {
        return this.selectOne(Wrappers.<SignRecordDO>lambdaQuery()
                .eq(SignRecordDO::getMemberId, memberId)
                .orderByDesc(SignRecordDO::getSignDate)
                .last("LIMIT 1"));
    }

    /**
     * 签到保存。
     */
    default int signSave(SignRecordDO value) {
        return this.insert(value);
    }

    /**
     * 查询签到记录。
     */
    default List<SignRecordDO> signs(
            Long memberId,
            int offset,
            int limit) {
        return this.selectPage(new Page<>((long) offset / limit + 1, limit, false),
                Wrappers.<SignRecordDO>lambdaQuery()
                .eq(SignRecordDO::getMemberId, memberId)
                .orderByDesc(SignRecordDO::getId)).getRecords();
    }

    /**
     * 签到数量。
     */
    default long signCount(Long memberId) {
        return this.selectCount(Wrappers.<SignRecordDO>lambdaQuery()
                .eq(SignRecordDO::getMemberId, memberId));
    }

    /**
     * 查询签到记录。
     */
    default List<SignRecordDO> signsPage(SignRecordPageFilter f) {
        return this.selectPage(new Page<>((long) f.offset() / f.limit() + 1, f.limit(), false),
                Wrappers.<SignRecordDO>lambdaQuery()
                .eq(f.memberId() != null, SignRecordDO::getMemberId, f.memberId())
                .orderByDesc(SignRecordDO::getId)).getRecords();
    }

    /**
     * 查询签到记录数量。
     */
    default long signsCount(SignRecordPageFilter f) {
        return this.selectCount(Wrappers.<SignRecordDO>lambdaQuery()
                .eq(f.memberId() != null, SignRecordDO::getMemberId, f.memberId()));
    }
}
