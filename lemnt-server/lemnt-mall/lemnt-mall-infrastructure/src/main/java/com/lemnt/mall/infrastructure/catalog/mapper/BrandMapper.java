package com.lemnt.mall.infrastructure.catalog.mapper;

import com.lemnt.mall.infrastructure.catalog.dataobject.BrandDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** Brand 表数据访问接口。 */
@Mapper
public interface BrandMapper extends BaseMapper<BrandDO> {
    /**
     * 查询品牌。
     */
    default List<BrandDO> brands() {
        return this.selectList(Wrappers.<BrandDO>lambdaQuery()
                .orderByAsc(BrandDO::getSort)
                .orderByAsc(BrandDO::getId)
                .last("LIMIT 1001"));
    }

    /**
     * 品牌。
     */
    default BrandDO brand(Long id) {
        return this.selectOne(Wrappers.<BrandDO>lambdaQuery()
                .eq(BrandDO::getId, id));
    }

    /**
     * 插入品牌。
     */
    default int insertBrand(BrandDO row) {
        return this.insert(row);
    }

    /**
     * 更新品牌。
     */
    default int updateBrand(BrandDO row) {
        return this.update(null, Wrappers.<BrandDO>lambdaUpdate()
                .set(BrandDO::getName, row.getName())
                .set(BrandDO::getPicUrl, row.getPicUrl())
                .set(BrandDO::getDescription, row.getDescription())
                .set(BrandDO::getSort, row.getSort())
                .set(BrandDO::getStatus, row.getStatus())
                .eq(BrandDO::getId, row.getId()));
    }

    /**
     * 删除品牌。
     */
    default int deleteBrand(Long id) {
        return this.delete(Wrappers.<BrandDO>lambdaQuery()
                .eq(BrandDO::getId, id));
    }
}
