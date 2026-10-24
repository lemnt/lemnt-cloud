package com.lemnt.system.domain.config.gateway;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.config.model.Config;

import java.util.List;

/**
 * 参数配置网关 — domain 层端口
 *
 * @author wangxx
 */
public interface ConfigGateway {

    /**
     * 根据参数ID查询信息
     *
     * @param configId 参数ID
     * @return 参数配置信息
     */
    Config selectById(Long configId);

    /**
     * 根据键名查询参数配置
     *
     * @param configKey 参数键名
     * @return 参数配置信息
     */
    Config selectByKey(String configKey);

    /**
     * 分页查询参数配置列表
     *
     * @param query   查询条件
     * @param pageQry 分页参数
     * @return 分页结果
     */
    PageResult<Config> selectPage(Config query, PageQry pageQry);

    /**
     * 新增参数配置
     *
     * @param entity 参数配置信息
     * @return 是否成功
     */
    Boolean insert(Config entity);

    /**
     * 修改参数配置
     *
     * @param entity 参数配置信息
     * @return 是否成功
     */
    Boolean update(Config entity);

    /**
     * 批量删除参数配置
     *
     * @param configIds 参数ID数组
     * @return 是否成功
     */
    Boolean deleteByIds(List<Long> configIds);

    /**
     * 清空参数缓存
     */
    void clearCache();

    /**
     * 清除指定键名的参数缓存
     *
     * @param configKey 参数键名
     */
    void clearCacheByKey(String configKey);

    /**
     * 重置参数缓存
     */
    Boolean resetCache();

    /**
     * 校验参数键名是否唯一
     *
     * @param configKey 参数键名
     * @return 参数配置信息
     */
    Config checkConfigKeyUnique(String configKey);

    /** 校验参数键名是否唯一，修改时排除当前参数。 */
    Config checkConfigKeyUnique(String configKey, Long excludeConfigId);
}
