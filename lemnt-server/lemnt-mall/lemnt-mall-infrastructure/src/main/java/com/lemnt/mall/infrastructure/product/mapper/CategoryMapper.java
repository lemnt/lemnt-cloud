package com.lemnt.mall.infrastructure.product.mapper;

import com.lemnt.mall.infrastructure.product.dataobject.CategoryDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** Category 表数据访问接口。 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryDO> {

    /**
     * 查询分类。
     */
    List<CategoryDO> storeCatalogCategories();
}
