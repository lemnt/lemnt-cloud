package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.CombinationRecordDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 拼团记录表 CRUD 及成团人数串行更新行锁。 */
@Mapper
public interface CombinationRecordMapper extends BaseMapper<CombinationRecordDO> {

    /** 锁定拼团记录。 */
    CombinationRecordDO lockById(Long id);
}
