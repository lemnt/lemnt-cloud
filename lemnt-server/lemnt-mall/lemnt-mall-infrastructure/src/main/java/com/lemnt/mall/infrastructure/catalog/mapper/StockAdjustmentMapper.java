package com.lemnt.mall.infrastructure.catalog.mapper;

import com.lemnt.mall.infrastructure.catalog.dataobject.StockAdjustmentDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.ibatis.annotations.Mapper;

/** StockAdjustment 表数据访问接口。 */
@Mapper
public interface StockAdjustmentMapper extends BaseMapper<StockAdjustmentDO> {
    /**
     * 调整。
     */
    default StockAdjustmentDO adjustment(String requestNo) {
        return this.selectOne(Wrappers.<StockAdjustmentDO>lambdaQuery()
                .eq(StockAdjustmentDO::getRequestNo, requestNo));
    }

    /**
     * 插入调整。
     */
    default int insertAdjustment(StockAdjustmentDO row) {
        return this.insert(row);
    }

    /**
     * 完成库存调整。
     */
    default int finishAdjustment(StockAdjustmentDO row) {
        return this.update(null, Wrappers.<StockAdjustmentDO>lambdaUpdate()
                .set(StockAdjustmentDO::getResultingStock, row.getResultingStock())
                .eq(StockAdjustmentDO::getRequestNo, row.getRequestNo()));
    }
}
