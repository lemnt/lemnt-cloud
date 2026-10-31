package com.lemnt.member.infrastructure.growth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lemnt.member.infrastructure.growth.dataobject.LevelDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 简单CRUD直接BaseMapper，跨表未使用校验删除在XML中。 */
@Mapper
public interface MemberLevelMapper extends BaseMapper<LevelDO> {
    /** 仅删除当前没有会员使用的等级，保留跨表原子条件。 */
    int levelDelete(@Param("id") Long id);
}
