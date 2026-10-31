package com.lemnt.member.infrastructure.growth.gateway;

import com.lemnt.member.domain.growth.gateway.MemberLevelRecordGateway;
import com.lemnt.member.domain.growth.model.LevelRecord;
import com.lemnt.member.domain.growth.model.LevelRecordPageFilter;
import com.lemnt.member.infrastructure.growth.converter.LevelRecordConverter;
import com.lemnt.member.infrastructure.growth.mapper.MemberLevelRecordMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** member_level_record 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class MemberLevelRecordGatewayImpl implements MemberLevelRecordGateway {

    private final MemberLevelRecordMapper memberLevelRecordMapper;

    /**
     * 等级记录。
     */
    @Override
    public void levelRecord(LevelRecord value) {
        memberLevelRecordMapper.insert(LevelRecordConverter.toDO(value));
    }

    /**
     * 等级记录。
     */
    @Override
    public List<LevelRecord> levelRecords(Long memberId, int offset, int limit) {
        return levels(new LevelRecordPageFilter(memberId, offset, limit));
    }

    /**
     * 等级记录数量。
     */
    @Override
    public long levelRecordCount(Long memberId) {
        return levelsCount(new LevelRecordPageFilter(memberId, 0, 1));
    }

    /**
     * 查询等级。
     */
    @Override
    public List<LevelRecord> levels(LevelRecordPageFilter f) {
        return memberLevelRecordMapper.levels(f).stream().map(LevelRecordConverter::toEntity).toList();
    }

    /**
     * 查询等级数量。
     */
    @Override
    public long levelsCount(LevelRecordPageFilter f) {
        return memberLevelRecordMapper.levelsCount(f);
    }

    /**
     * 查询等级详情。
     */
    @Override
    public LevelRecord levelsDetail(Long id) {
        return LevelRecordConverter.toEntity(memberLevelRecordMapper.selectById(id));
    }
}
