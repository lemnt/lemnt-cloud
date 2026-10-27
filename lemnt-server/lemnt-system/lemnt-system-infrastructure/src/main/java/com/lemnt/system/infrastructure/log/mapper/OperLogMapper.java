package com.lemnt.system.infrastructure.log.mapper;

import com.lemnt.system.domain.log.model.OperLog;
import com.lemnt.system.infrastructure.log.dataobject.OperLogDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * OperLog 数据层
 *
 * @author wangxx
 */
@Mapper
public interface OperLogMapper extends BaseMapper<OperLogDO> {
    /**
     * 分页查询系统操作日志
     *
     * @param page    分页参数
     * @param operLog 操作日志对象
     * @return 分页结果
     */
    IPage<OperLogDO> selectPage(Page<OperLogDO> page, @Param("qry") OperLog operLog);

    /**
     * 清空操作日志
     */
    void cleanOperLog();
}
