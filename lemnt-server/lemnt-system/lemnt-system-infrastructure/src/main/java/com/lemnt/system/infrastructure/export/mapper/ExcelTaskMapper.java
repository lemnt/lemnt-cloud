package com.lemnt.system.infrastructure.export.mapper;

import com.lemnt.system.infrastructure.export.dataobject.ExcelTaskDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 格任务Mapper接口
 *
 * @author wangxx
 * @date 2026-01-20
 */
@Mapper
public interface ExcelTaskMapper extends BaseMapper<ExcelTaskDO> {
}
