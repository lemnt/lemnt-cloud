package com.lemnt.promotion.infrastructure.promotion.mapper;

import com.lemnt.promotion.infrastructure.promotion.dataobject.BannerDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Banner 表 CRUD。
 */
@Mapper
public interface BannerMapper extends BaseMapper<BannerDO> {
}
