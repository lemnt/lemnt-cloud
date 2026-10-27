package com.lemnt.member.infrastructure.growth.mapper;

import com.lemnt.member.infrastructure.growth.dataobject.LevelDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** member_level 表数据访问接口。 */
@Mapper
public interface MemberLevelMapper extends BaseMapper<LevelDO> {

    /**
     * 查询等级。
     */
    default List<LevelDO> levels() {
        return this.selectList(Wrappers.<LevelDO>lambdaQuery()
                .orderByAsc(LevelDO::getLevel));
    }

    /**
     * 等级。
     */
    default LevelDO level(Long id) {
        return this.selectOne(Wrappers.<LevelDO>lambdaQuery()
                .eq(LevelDO::getId, id));
    }

    /**
     * 等级插入。
     */
    default int levelInsert(LevelDO value) {
        return this.insert(value);
    }

    /**
     * 等级更新。
     */
    default int levelUpdate(LevelDO value) {
        return this.update(null, Wrappers.<LevelDO>lambdaUpdate()
                .set(LevelDO::getName, value.getName())
                .set(LevelDO::getLevel, value.getLevel())
                .set(LevelDO::getExperience, value.getExperience())
                .set(LevelDO::getDiscountPercent, value.getDiscountPercent())
                .set(LevelDO::getIcon, value.getIcon())
                .set(LevelDO::getBackgroundUrl, value.getBackgroundUrl())
                .set(LevelDO::getStatus, value.getStatus())
                .eq(LevelDO::getId, value.getId()));
    }

    /**
     * 等级删除。
     */
    @Delete(
            "DELETE FROM member_level WHERE id=#{id} AND NOT EXISTS(SELECT 1 FROM member_user WHERE"
            + " level_id=#{id})")
    int levelDelete(Long id);
}
