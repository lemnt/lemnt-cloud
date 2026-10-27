package com.lemnt.mall.infrastructure.catalog.mapper;

import com.lemnt.mall.infrastructure.catalog.dataobject.SkuPropertyDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** SkuProperty 表数据访问接口。 */
@Mapper
public interface SkuPropertyMapper extends BaseMapper<SkuPropertyDO> {
    /**
     * 插入属性。
     */
    default int insertProperty(SkuPropertyDO row) {
        return this.insert(row);
    }

    /**
     * 配置属性按商品SPU。
     */
    @Select(
            "SELECT p.* FROM mall_sku_property p JOIN mall_product s ON s.id=p.product_id WHERE"
            + " s.spu_id=#{id} ORDER BY p.property_name")
    List<SkuPropertyDO> propertiesBySpu(Long id);

    /**
     * 配置属性。
     */
    default List<SkuPropertyDO> properties(Long id) {
        return this.selectList(Wrappers.<SkuPropertyDO>lambdaQuery()
                .eq(SkuPropertyDO::getProductId, id)
                .orderByAsc(SkuPropertyDO::getPropertyName));
    }
}
