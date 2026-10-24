package com.lemnt.system.infrastructure.config.mapper;

import com.lemnt.system.domain.config.model.DictType;
import com.lemnt.system.infrastructure.config.dataobject.DictTypeDO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.Arrays;
import java.util.List;

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

    /**
     * 根据所有字典类型
     *
     * @return 字典类型集合信息
     */
    default List<DictTypeDO> selectDictTypeAll() {
        return this.selectList(Wrappers.<DictTypeDO>lambdaQuery());
    }

    /**
     * 根据字典类型查询信息
     *
     * @param dictType 字典类型
     * @return 字典类型
     */
    default DictTypeDO selectDictTypeByType(String dictType) {
        return this.selectOne(Wrappers.<DictTypeDO>lambdaQuery()
                .eq(DictTypeDO::getDictType, dictType));
    }

    /**
     * 校验字典类型称是否唯一
     *
     * @param dictType 字典类型
     * @return 结果
     */
    default DictTypeDO checkDictTypeUnique(String dictType) {
        return this.selectOne(Wrappers.<DictTypeDO>lambdaQuery()
                .eq(DictTypeDO::getDictType, dictType).last("limit 1"));
    }
}
