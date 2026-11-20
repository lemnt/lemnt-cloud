package com.lemnt.system.infrastructure.config.mapper;

import com.lemnt.system.domain.config.model.DictData;
import com.lemnt.system.infrastructure.config.dataobject.DictDataDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * DictData 数据层
 *
 * @author wangxx
 */
@Mapper
public interface DictDataMapper extends BaseMapper<DictDataDO> {
    /**
     * 分页查询字典数据列表
     *
     * @param page     分页参数
     * @param dictData 查询条件
     * @return 分页结果
     */
    IPage<DictDataDO> selectPage(Page<DictDataDO> page, @Param("qry") DictData dictData);
}
