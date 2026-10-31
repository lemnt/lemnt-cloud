package com.lemnt.mall.infrastructure.promotion.mapper;

import com.lemnt.mall.infrastructure.promotion.dataobject.SeckillActivityDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 秒杀活动表 CRUD 及包含 JSON 时段条件的查询。 */
@Mapper
public interface SeckillActivityMapper extends BaseMapper<SeckillActivityDO> {

    /** 锁定活动行。 */
    SeckillActivityDO lockById(Long id);

    /** 后台分页，时段编号以逗号存储。 */
    IPage<SeckillActivityDO> selectPageByFilter(IPage<SeckillActivityDO> page,
            @Param("name") String name, @Param("status") Integer status, @Param("configId") Long configId);

    /** 查询指定 SPU 的开启活动。 */
    List<SeckillActivityDO> selectEnabledBySpuIds(@Param("spuIds") List<Long> spuIds);

    /** App 分页。 */
    IPage<SeckillActivityDO> selectAppPage(IPage<SeckillActivityDO> page,
            @Param("configId") Long configId, @Param("now") java.time.LocalDateTime now);

    /** 指定时段的进行中活动。 */
    List<SeckillActivityDO> selectByConfigAndNow(@Param("configId") Long configId,
            @Param("now") java.time.LocalDateTime now);
}
