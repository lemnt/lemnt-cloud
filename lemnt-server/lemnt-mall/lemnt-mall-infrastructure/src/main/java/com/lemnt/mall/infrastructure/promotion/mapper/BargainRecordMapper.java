package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.BargainRecordDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 砍价记录表 CRUD。 */
@Mapper
public interface BargainRecordMapper extends BaseMapper<BargainRecordDO> {
}
