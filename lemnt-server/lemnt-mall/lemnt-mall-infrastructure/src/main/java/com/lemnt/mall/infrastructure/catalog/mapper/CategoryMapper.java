package com.lemnt.mall.infrastructure.catalog.mapper;

import com.lemnt.mall.infrastructure.catalog.dataobject.CategoryDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** Category 表数据访问接口。 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryDO> {
    /**
     * 查询分类。
     */
    default List<CategoryDO> catalogCategories() {
        return this.selectList(Wrappers.<CategoryDO>lambdaQuery()
                .orderByAsc(CategoryDO::getSort)
                .orderByAsc(CategoryDO::getId)
                .last("LIMIT 1001"));
    }

    /**
     * 分类。
     */
    default CategoryDO category(Long id) {
        return this.selectOne(Wrappers.<CategoryDO>lambdaQuery()
                .eq(CategoryDO::getId, id));
    }

    /**
     * 插入分类。
     */
    default int insertCategory(CategoryDO row) {
        return this.insert(row);
    }

    /**
     * 更新分类。
     */
    default int updateCategory(CategoryDO row) {
        return this.update(null, Wrappers.<CategoryDO>lambdaUpdate()
                .set(CategoryDO::getName, row.getName())
                .set(CategoryDO::getPicUrl, row.getPicUrl())
                .set(CategoryDO::getSort, row.getSort())
                .set(CategoryDO::getStatus, row.getStatus())
                .eq(CategoryDO::getId, row.getId()));
    }

    /**
     * 删除分类。
     */
    default int deleteCategory(Long id) {
        return this.delete(Wrappers.<CategoryDO>lambdaQuery()
                .eq(CategoryDO::getId, id));
    }

    /**
     * 查询分类。
     */
    @Select(
            "SELECT c.* FROM mall_category c LEFT JOIN mall_category p ON p.id=c.parent_id WHERE"
            + " c.status='ENABLED' AND (c.parent_id IS NULL OR p.status='ENABLED') ORDER BY"
            + " c.sort,c.id LIMIT 1001")
    List<CategoryDO> storeCatalogCategories();
}
