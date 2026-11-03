package com.lemnt.member.infrastructure.growth.gateway;

import com.lemnt.member.domain.growth.gateway.MemberLevelRecordGateway;
import com.lemnt.member.domain.growth.model.LevelRecord;
import com.lemnt.member.domain.growth.model.LevelRecordPageFilter;
import com.lemnt.member.infrastructure.growth.converter.LevelRecordConverter;
import com.lemnt.member.infrastructure.growth.dataobject.LevelRecordDO;
import com.lemnt.member.infrastructure.growth.mapper.MemberLevelRecordMapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
        return levelsRow(f).stream().map(LevelRecordConverter::toEntity).toList();
    }

    /**
     * 查询等级数量。
     */
    @Override
    public long levelsCount(LevelRecordPageFilter f) {
        return levelsCountRow(f);
    }

    /**
     * 查询等级详情。
     */
    @Override
    public LevelRecord levelsDetail(Long id) {
        return LevelRecordConverter.toEntity(memberLevelRecordMapper.selectById(id));
    }

    /** 等级记录分页，按记录编号倒序。 */
    private List<LevelRecordDO> levelsRow(LevelRecordPageFilter filter) {
        return memberLevelRecordMapper
                .selectPage(
                        new Page<>(
                                (long) filter.offset() / filter.limit() + 1, filter.limit(), false),
                        conditions(filter).orderByDesc(LevelRecordDO::getId))
                .getRecords();
    }

    /** 与分页共用完全相同的筛选条件。 */
    private long levelsCountRow(LevelRecordPageFilter filter) {
        return memberLevelRecordMapper.selectCount(conditions(filter));
    }

    /** 分页与计数复用记录当时的编号和时间，不关联当前等级配置。 */
    private LambdaQueryWrapper<LevelRecordDO> conditions(LevelRecordPageFilter filter) {
        return Wrappers.<LevelRecordDO>lambdaQuery()
                .eq(filter.memberId() != null, LevelRecordDO::getUserId, filter.memberId())
                .eq(filter.levelId() != null, LevelRecordDO::getLevelId, filter.levelId())
                .ge(filter.startTime() != null, LevelRecordDO::getCreateTime, filter.startTime())
                .le(filter.endTime() != null, LevelRecordDO::getCreateTime, filter.endTime());
    }
}
