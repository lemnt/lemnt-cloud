package com.lemnt.system.domain.config.gateway;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.config.model.DictType;

import java.util.List;

/**
 * 字典类型网关 — domain 层端口
 *
 * @author wangxx
 */
public interface DictTypeGateway {
    /**
     * 根据字典类型ID查询信息
     *
     * @param dictId 字典类型ID
     * @return 字典类型信息
     */
    DictType selectById(Long dictId);

    /**
     * 根据字典类型查询信息
     *
     * @param dictType 字典类型
     * @return 字典类型信息
     */
    DictType selectByType(String dictType);

    /**
     * 分页查询字典类型列表
     *
     * @param query   查询条件
     * @param pageQry 分页参数
     * @return 分页结果
     */
    PageResult<DictType> selectPage(DictType query, PageQry pageQry);

    /**
     * 查询所有字典类型
     *
     * @return 字典类型集合
     */
    List<DictType> selectAll();

    /**
     * 新增字典类型
     *
     * @param entity 字典类型信息
     * @return 是否成功
     */
    Boolean insert(DictType entity);

    /**
     * 修改字典类型
     *
     * @param entity 字典类型信息
     * @return 是否成功
     */
    Boolean update(DictType entity);

    /**
     * 批量删除字典类型
     *
     * @param dictIds 字典类型ID数组
     * @return 是否成功
     */
    Boolean deleteByIds(List<Long> dictIds);

    /**
     * 校验字典类型是否唯一
     *
     * @param dictType 字典类型
     * @return 字典类型信息
     */
    DictType checkDictTypeUnique(String dictType);
}
