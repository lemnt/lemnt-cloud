package com.lemnt.system.infrastructure.config.gateway;

import java.util.List;
import java.util.Set;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.redis.service.RedisStringService;
import com.lemnt.system.domain.config.model.Config;
import com.lemnt.system.domain.config.gateway.ConfigGateway;
import com.lemnt.system.infrastructure.config.converter.ConfigConverter;
import com.lemnt.system.infrastructure.config.dataobject.ConfigDO;
import com.lemnt.system.infrastructure.config.mapper.ConfigMapper;

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
        return ConfigConverter.toEntity(configMapper.selectConfig(configId, null));
    }

    @Override
    public Config selectByKey(String configKey) {
        return ConfigConverter.toEntity(configMapper.selectConfig(null, configKey));
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
        return ConfigConverter.toEntity(configMapper.checkConfigKeyUnique(configKey));
    }

    @Override
    public Config checkConfigKeyUnique(String configKey, Long excludeConfigId) {
        return ConfigConverter.toEntity(configMapper.checkConfigKeyUnique(configKey, excludeConfigId));
    }

    private String getCacheKey(String configKey) {
        return CacheConstants.SYS_CONFIG_KEY + configKey;
    }
}
