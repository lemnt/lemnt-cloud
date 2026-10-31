package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.SeckillConfigDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** 秒杀时段表 CRUD 及时间冲突校验所需行锁。 */
@Mapper
public interface SeckillConfigMapper extends BaseMapper<SeckillConfigDO> {

    /** 锁定全部时段，串行创建/修改时段。 */
    List<SeckillConfigDO> lockAll();
}
