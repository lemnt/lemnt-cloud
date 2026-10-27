package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionVersionDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

/** PromotionVersion 表数据访问接口。 */
@Mapper
public interface PromotionVersionMapper extends BaseMapper<PromotionVersionDO> {
    /**
     * 插入版本。
     */
    default int insertVersion(PromotionVersionDO row) {
        return this.insert(row);
    }

    /**
     * 版本。
     */
    default PromotionVersionDO version(long root, int version) {
        return this.selectOne(Wrappers.<PromotionVersionDO>lambdaQuery()
                .eq(PromotionVersionDO::getRootId, root)
                .eq(PromotionVersionDO::getVersion, version));
    }
}
