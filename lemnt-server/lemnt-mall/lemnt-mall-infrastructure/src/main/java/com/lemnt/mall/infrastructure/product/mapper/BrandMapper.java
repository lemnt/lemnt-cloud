package com.lemnt.mall.infrastructure.product.mapper;

import com.lemnt.mall.infrastructure.product.dataobject.BrandDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** Brand 表数据访问接口。 */
@Mapper
public interface BrandMapper extends BaseMapper<BrandDO> {
}
