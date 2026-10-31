package com.lemnt.system.domain.config.gateway;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.config.model.DictData;

import java.util.List;

/**
 * 字典数据网关 — domain 层端口
 *
 * @author wangxx
 */
public interface DictDataGateway {
    /**
     * 根据字典数据ID查询信息
     *
     * @param dictCode 字典数据ID
     * @return 字典数据信息
     */
    DictData selectById(Long dictCode);

    /**
     * 分页查询字典数据列表
     *
     * @param query   查询条件
     * @param pageQry 分页参数
     * @return 分页结果
     */
    PageResult<DictData> selectPage(DictData query, PageQry pageQry);

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合
     */
    List<DictData> selectByType(String dictType);

    /**
     * 新增字典数据
     *
     * @param entity 字典数据信息
     * @return 是否成功
     */
    Boolean insert(DictData entity);

    /**
     * 修改字典数据
     *
     * @param entity 字典数据信息
     * @return 是否成功
     */
    Boolean update(DictData entity);

    /**
     * 批量删除字典数据
     *
     * @param dictCodes 字典数据ID数组
     * @return 是否成功
     */
    Boolean deleteByIds(List<Long> dictCodes);

    /**
     * 根据字典类型删除字典数据
     *
     * @param dictType 字典类型
     */
    Boolean deleteByType(String dictType);

    /**
     * 查询指定字典类型的数据数量
     *
     * @param dictType 字典类型
     * @return 数量
     */
    Long countDictDataByType(String dictType);

    /**
     * 清空字典缓存
     */
    void clearCache();

    /**
     * 重置字典缓存
     */
    void resetCache();

    /**
     * 清除指定类型的字典缓存
     *
     * @param dictType 字典类型
     */
    void clearCacheByType(String dictType);

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合
     */
    List<DictData> selectDictDataByType(String dictType);

    /** 修改字典项引用的类型名称。 */
    void renameType(String oldType, String newType);

    /** 从数据库刷新单个字典的数据缓存。 */
    void refreshByType(String type);
}
