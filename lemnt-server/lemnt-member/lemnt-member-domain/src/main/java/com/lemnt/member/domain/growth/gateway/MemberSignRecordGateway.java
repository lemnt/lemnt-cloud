package com.lemnt.member.domain.growth.gateway;

import com.lemnt.member.domain.growth.model.SignRecord;
import com.lemnt.member.domain.growth.model.SignRecordPageFilter;

import java.time.LocalDate;
import java.util.List;

/** member_sign_in_record 表领域存储网关。 */
public interface MemberSignRecordGateway {

    /**
     * 签到日期。
     */
    SignRecord signDate(Long memberId, LocalDate date);

    /**
     * 最近一次签到。
     */
    SignRecord lastSign(Long memberId);

    /**
     * 保存签到并返回生成编号，供奖励流水关联。
     */
    Long signSave(SignRecord value);

    /**
     * 查询签到记录。
     */
    List<SignRecord> signs(Long memberId, int offset, int limit);

    /**
     * 签到数量。
     */
    long signCount(Long memberId);

    /**
     * 查询签到记录。
     */
    List<SignRecord> signs(SignRecordPageFilter filter);

    /**
     * 查询签到记录数量。
     */
    long signsCount(SignRecordPageFilter filter);
}
