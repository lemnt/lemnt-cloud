package com.lemnt.system.infrastructure.config.mapper;

import com.lemnt.system.domain.config.model.DictType;
import com.lemnt.system.infrastructure.config.dataobject.DictTypeDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * DictType 数据层
 *
 * @author wangxx
 */
@Mapper
public interface DictTypeMapper extends BaseMapper<DictTypeDO> {
    /**
     * 分页查询字典类型列表
     *
     * @param page     分页参数
     * @param dictType 查询条件
     * @return 分页结果
     */
    IPage<DictTypeDO> selectPage(Page<DictTypeDO> page, @Param("qry") DictType dictType);
}
