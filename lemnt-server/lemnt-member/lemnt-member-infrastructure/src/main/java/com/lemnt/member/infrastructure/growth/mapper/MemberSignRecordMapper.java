package com.lemnt.member.infrastructure.growth.mapper;

import com.lemnt.member.domain.growth.model.SignRecordPageFilter;
import com.lemnt.member.infrastructure.growth.dataobject.SignRecordDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/** member_sign_in_record 表数据访问接口。 */
@Mapper
public interface MemberSignRecordMapper extends BaseMapper<SignRecordDO> {

    /**
     * 签到日期。
     */
    default SignRecordDO signDate(Long memberId, LocalDate date) {
        return this.selectOne(Wrappers.<SignRecordDO>lambdaQuery()
                .eq(SignRecordDO::getUserId, memberId)
                .eq(SignRecordDO::getSignDate, date));
    }

    /**
     * 最近一次签到。
     */
    default SignRecordDO lastSign(Long memberId) {
        return this.selectOne(Wrappers.<SignRecordDO>lambdaQuery()
                .eq(SignRecordDO::getUserId, memberId)
                .orderByDesc(SignRecordDO::getSignDate)
                .last("LIMIT 1"));
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
                .eq(SignRecordDO::getUserId, memberId)
                .orderByDesc(SignRecordDO::getId)).getRecords();
    }

    /**
     * 签到数量。
     */
    default long signCount(Long memberId) {
        return this.selectCount(Wrappers.<SignRecordDO>lambdaQuery()
                .eq(SignRecordDO::getUserId, memberId));
    }

    /**
     * 查询签到记录。
     */
    List<SignRecordDO> signsPage(@org.apache.ibatis.annotations.Param("f") SignRecordPageFilter f);
    /** 总数与关联分页使用相同条件。 */
    long signsCount(@org.apache.ibatis.annotations.Param("f") SignRecordPageFilter f);
}
