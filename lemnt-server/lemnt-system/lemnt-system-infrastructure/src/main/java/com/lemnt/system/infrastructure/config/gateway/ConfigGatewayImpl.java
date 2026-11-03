package com.lemnt.system.infrastructure.config.gateway;

import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.redis.service.RedisStringService;
import com.lemnt.system.domain.config.gateway.ConfigGateway;
import com.lemnt.system.domain.config.model.Config;
import com.lemnt.system.infrastructure.config.converter.ConfigConverter;
import com.lemnt.system.infrastructure.config.dataobject.ConfigDO;
import com.lemnt.system.infrastructure.config.mapper.ConfigMapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 参数配置网关实现 — infrastructure 层适配器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class ConfigGatewayImpl implements ConfigGateway {
    private final ConfigMapper configMapper;
    private final RedisStringService redisStringService;

    /**
     * 项目启动时，初始化参数到缓存
     */
    @PostConstruct
    public void init() {
        resetCache();
    }

    @Override
    public Config selectById(Long configId) {
        return ConfigConverter.toEntity(selectConfigRow(configId, null));
    }

    @Override
    public Config selectByKey(String configKey) {
        return ConfigConverter.toEntity(selectConfigRow(null, configKey));
    }

    @Override
    public PageResult<Config> selectPage(Config query, PageQry pageQry) {
        Page<ConfigDO> page = new Page<>(pageQry.getPageNum(), pageQry.getPageSize());
        IPage<ConfigDO> result = configMapper.selectPage(page, query);
        List<Config> records = ConfigConverter.toEntityList(result.getRecords());
        return PageResult.of(records, result.getTotal());
    }

    @Override
    public Boolean insert(Config entity) {
        ConfigDO d = ConfigConverter.toDO(entity);
        int row = configMapper.insert(d);
        if (row > 0) {
            redisStringService.set(getCacheKey(entity.getConfigKey()), entity.getConfigValue());
        }
        return row > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBusinessConfig(Config entity) {
        configMapper.saveBusinessConfig(entity);
        // 配置读取直接访问权威库，清除旧缓存，避免保留先前的业务配置值。
        redisStringService.delete(getCacheKey(entity.getConfigKey()));
    }

    @Override
    public Boolean update(Config entity) {
        ConfigDO old = configMapper.selectById(entity.getConfigId());
        if (old != null && !old.getConfigKey().equals(entity.getConfigKey())) {
            redisStringService.delete(getCacheKey(old.getConfigKey()));
        }
        int row = configMapper.updateById(ConfigConverter.toDO(entity));
        if (row > 0) {
            redisStringService.set(getCacheKey(entity.getConfigKey()), entity.getConfigValue());
        }
        return row > 0;
    }

    @Override
    public Boolean deleteByIds(List<Long> configIds) {
        for (Long configId : configIds) {
            ConfigDO d = configMapper.selectById(configId);
            if (d != null) {
                redisStringService.delete(getCacheKey(d.getConfigKey()));
            }
        }
        return configMapper.deleteByIds(configIds) > 0;
    }

    @Override
    public void clearCache() {
        Set<String> keys = redisStringService.keys(CacheConstants.SYS_CONFIG_KEY + "*");
        for (String key : keys) {
            redisStringService.delete(key);
        }
    }

    @Override
    public void clearCacheByKey(String configKey) {
        redisStringService.delete(getCacheKey(configKey));
    }

    @Override
    public Boolean resetCache() {
        clearCache();
        List<ConfigDO> list = configMapper.selectList(null);
        for (ConfigDO config : list) {
            redisStringService.set(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
        return true;
    }

    @Override
    public Config checkConfigKeyUnique(String configKey) {
        return ConfigConverter.toEntity(checkConfigKeyUniqueRow(configKey));
    }

    @Override
    public Config checkConfigKeyUnique(String configKey, Long excludeConfigId) {
        return ConfigConverter.toEntity(checkConfigKeyUniqueRow(configKey, excludeConfigId));
    }

    private String getCacheKey(String configKey) {
        return CacheConstants.SYS_CONFIG_KEY + configKey;
    }

    /**
     * 查询参数配置信息
     *
     * @param configId  参数配置信息
     * @param configKey 参数配置键
     * @return 参数配置信息
     */
    private ConfigDO selectConfigRow(Long configId, String configKey) {
        return configMapper.selectOne(
                Wrappers.<ConfigDO>lambdaQuery()
                        .eq(Objects.nonNull(configId), ConfigDO::getConfigId, configId)
                        .eq(StringUtils.isNotBlank(configKey), ConfigDO::getConfigKey, configKey));
    }

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数键名
     * @return 参数配置信息
     */
    private ConfigDO checkConfigKeyUniqueRow(String configKey) {
        return checkConfigKeyUniqueRow(configKey, null);
    }

    /** 排除自身后查冲突，避免已有重复数据中先查到自身而漏判。 */
    private ConfigDO checkConfigKeyUniqueRow(String configKey, Long excludeConfigId) {
        return configMapper.selectOne(
                Wrappers.<ConfigDO>lambdaQuery()
                        .eq(ConfigDO::getConfigKey, configKey)
                        .ne(excludeConfigId != null, ConfigDO::getConfigId, excludeConfigId)
                        .last("limit 1"));
    }
}
