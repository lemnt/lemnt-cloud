package com.lemnt.member.infrastructure.growth.gateway;

import com.lemnt.member.domain.growth.gateway.MemberSignRecordGateway;
import com.lemnt.member.domain.growth.model.SignRecord;
import com.lemnt.member.domain.growth.model.SignRecordPageFilter;
import com.lemnt.member.infrastructure.growth.converter.SignRecordConverter;
import com.lemnt.member.infrastructure.growth.mapper.MemberSignRecordMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/** member_sign_in_record 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class MemberSignRecordGatewayImpl implements MemberSignRecordGateway {

    private final MemberSignRecordMapper memberSignRecordMapper;

    /**
     * 签到日期。
     */
    @Override
    public SignRecord signDate(Long memberId, LocalDate date) {
        return SignRecordConverter.toEntity(memberSignRecordMapper.signDate(memberId, date));
    }

    /**
     * 最近一次签到。
     */
    @Override
    public SignRecord lastSign(Long memberId) {
        return SignRecordConverter.toEntity(memberSignRecordMapper.lastSign(memberId));
    }

    /**
     * 签到保存。
     */
    @Override
    public void signSave(SignRecord value) {
        memberSignRecordMapper.insert(SignRecordConverter.toDO(value));
    }

    /**
     * 查询签到记录。
     */
    @Override
    public List<SignRecord> signs(Long memberId, int offset, int limit) {
        return memberSignRecordMapper.signs(memberId, offset, limit).stream()
                .map(SignRecordConverter::toEntity)
                .toList();
    }

    /**
     * 签到数量。
     */
    @Override
    public long signCount(Long memberId) {
        return memberSignRecordMapper.signCount(memberId);
    }

    /**
     * 查询签到记录。
     */
    @Override
    public List<SignRecord> signs(SignRecordPageFilter f) {
        return memberSignRecordMapper.signsPage(f).stream().map(SignRecordConverter::toEntity).toList();
    }

    /**
     * 查询签到记录数量。
     */
    @Override
    public long signsCount(SignRecordPageFilter f) {
        return memberSignRecordMapper.signsCount(f);
    }
}
