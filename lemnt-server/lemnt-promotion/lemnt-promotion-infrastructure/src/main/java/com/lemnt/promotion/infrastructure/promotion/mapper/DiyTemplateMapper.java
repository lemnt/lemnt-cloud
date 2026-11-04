package com.lemnt.promotion.infrastructure.promotion.mapper;

import com.lemnt.promotion.infrastructure.promotion.dataobject.DiyTemplateDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 装修模板表 CRUD。
 */
@Mapper
public interface DiyTemplateMapper extends BaseMapper<DiyTemplateDO> {
}
