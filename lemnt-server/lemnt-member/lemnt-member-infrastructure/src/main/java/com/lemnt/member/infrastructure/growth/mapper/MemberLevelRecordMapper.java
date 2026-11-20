package com.lemnt.member.infrastructure.growth.mapper;

import com.lemnt.member.infrastructure.growth.dataobject.LevelRecordDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * member_level_record 表查询；简单CRUD直接使用BaseMapper。
 */
@Mapper
public interface MemberLevelRecordMapper extends BaseMapper<LevelRecordDO> {
}
