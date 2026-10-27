package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionScopeDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/** PromotionScope 表数据访问接口。 */
@Mapper
public interface PromotionScopeMapper extends BaseMapper<PromotionScopeDO> {
    /**
     * 插入范围。
     */
    default int insertScope(long root, int version, String type, long target) {
        return this.insert(new PromotionScopeDO()
                .setRootId(root)
                .setVersion(version)
                .setType(type)
                .setTargetId(target));
    }
}
