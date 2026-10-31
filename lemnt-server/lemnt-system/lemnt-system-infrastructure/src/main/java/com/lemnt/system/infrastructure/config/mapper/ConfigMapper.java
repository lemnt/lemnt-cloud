package com.lemnt.system.infrastructure.config.mapper;

import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.system.domain.config.model.Config;
import com.lemnt.system.infrastructure.config.dataobject.ConfigDO;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Objects;

/**
 * Config 数据层
 *
 * @author wangxx
 */
@Mapper
public interface ConfigMapper extends BaseMapper<ConfigDO> {
    /**
     * 查询参数配置信息
     *
     * @param configId  参数配置信息
     * @param configKey 参数配置键
     * @return 参数配置信息
     */
    default ConfigDO selectConfig(Long configId, String configKey) {
        return this.selectOne(Wrappers.<ConfigDO>lambdaQuery()
                .eq(Objects.nonNull(configId), ConfigDO::getConfigId, configId)
                .eq(StringUtils.isNotBlank(configKey), ConfigDO::getConfigKey, configKey));
    }

    /**
     * 分页查询参数配置列表
     *
     * @param page   分页参数
     * @param config 查询条件
     * @return 分页结果
     */
    IPage<ConfigDO> selectPage(Page<ConfigDO> page, @Param("qry") Config config);

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数键名
     * @return 参数配置信息
     */
    default ConfigDO checkConfigKeyUnique(String configKey) {
        return checkConfigKeyUnique(configKey, null);
    }

    /** 排除自身后查冲突，避免已有重复数据中先查到自身而漏判。 */
    default ConfigDO checkConfigKeyUnique(String configKey, Long excludeConfigId) {
        return this.selectOne(Wrappers.<ConfigDO>lambdaQuery()
                .eq(ConfigDO::getConfigKey, configKey)
                .ne(excludeConfigId != null, ConfigDO::getConfigId, excludeConfigId)
                .last("limit 1"));
    }
}
