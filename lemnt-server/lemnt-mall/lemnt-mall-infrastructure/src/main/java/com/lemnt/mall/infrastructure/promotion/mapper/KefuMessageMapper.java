package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.KefuMessageDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** 客服消息表 CRUD。 */
@Mapper
public interface KefuMessageMapper extends BaseMapper<KefuMessageDO> {
}
