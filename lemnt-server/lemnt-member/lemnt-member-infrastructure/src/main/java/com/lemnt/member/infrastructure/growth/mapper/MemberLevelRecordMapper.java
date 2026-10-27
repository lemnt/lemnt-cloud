package com.lemnt.member.infrastructure.growth.mapper;

import com.lemnt.member.domain.growth.model.LevelRecordPageFilter;
import com.lemnt.member.infrastructure.growth.dataobject.LevelRecordDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** member_level_record 表数据访问接口。 */
@Mapper
public interface MemberLevelRecordMapper extends BaseMapper<LevelRecordDO> {

    /**
     * 等级记录。
     */
    default int levelRecord(LevelRecordDO value) {
        return this.insert(value);
    }

    /**
     * 等级记录。
     */
    default List<LevelRecordDO> levelRecords(
            Long memberId,
            int offset,
            int limit) {
        return this.selectPage(new Page<>((long) offset / limit + 1, limit, false),
                Wrappers.<LevelRecordDO>lambdaQuery()
                .eq(LevelRecordDO::getMemberId, memberId)
                .orderByDesc(LevelRecordDO::getId)).getRecords();
    }

    /**
     * 等级记录数量。
     */
    default long levelRecordCount(Long memberId) {
        return this.selectCount(Wrappers.<LevelRecordDO>lambdaQuery()
                .eq(LevelRecordDO::getMemberId, memberId));
    }

    /**
     * 查询等级。
     */
    default List<LevelRecordDO> levels(LevelRecordPageFilter f) {
        return this.selectPage(new Page<>((long) f.offset() / f.limit() + 1, f.limit(), false),
                Wrappers.<LevelRecordDO>lambdaQuery()
                .eq(f.memberId() != null, LevelRecordDO::getMemberId, f.memberId())
                .orderByDesc(LevelRecordDO::getId)).getRecords();
    }

    /**
     * 查询等级数量。
     */
    default long levelsCount(LevelRecordPageFilter f) {
        return this.selectCount(Wrappers.<LevelRecordDO>lambdaQuery()
                .eq(f.memberId() != null, LevelRecordDO::getMemberId, f.memberId()));
    }

    /**
     * 查询等级详情。
     */
    default LevelRecordDO levelsDetail(Long id) {
        return this.selectOne(Wrappers.<LevelRecordDO>lambdaQuery()
                .eq(LevelRecordDO::getId, id));
    }
}
