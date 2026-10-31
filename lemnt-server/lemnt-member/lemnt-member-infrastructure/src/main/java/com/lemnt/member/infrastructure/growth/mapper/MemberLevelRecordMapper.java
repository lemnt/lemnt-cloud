package com.lemnt.member.infrastructure.growth.mapper;

import com.lemnt.member.domain.growth.model.LevelRecordPageFilter;
import com.lemnt.member.infrastructure.growth.dataobject.LevelRecordDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** member_level_record 表查询；简单CRUD直接使用BaseMapper。 */
@Mapper
public interface MemberLevelRecordMapper extends BaseMapper<LevelRecordDO> {

    /** 等级记录分页，按记录编号倒序。 */
    default List<LevelRecordDO> levels(LevelRecordPageFilter filter) {
        return selectPage(new Page<>((long) filter.offset() / filter.limit() + 1, filter.limit(), false),
                conditions(filter).orderByDesc(LevelRecordDO::getId)).getRecords();
    }

    /** 与分页共用完全相同的筛选条件。 */
    default long levelsCount(LevelRecordPageFilter filter) {
        return selectCount(conditions(filter));
    }

    /** 只使用记录当时的编号及时间，不关联当前等级配置。 */
    private LambdaQueryWrapper<LevelRecordDO> conditions(LevelRecordPageFilter filter) {
        return Wrappers.<LevelRecordDO>lambdaQuery()
                .eq(filter.memberId() != null, LevelRecordDO::getUserId, filter.memberId())
                .eq(filter.levelId() != null, LevelRecordDO::getLevelId, filter.levelId())
                .ge(filter.startTime() != null, LevelRecordDO::getCreateTime, filter.startTime())
                .le(filter.endTime() != null, LevelRecordDO::getCreateTime, filter.endTime());
    }
}
