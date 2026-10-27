package com.lemnt.system.infrastructure.config.gateway;

import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.redis.service.RedisStringService;
import com.lemnt.system.domain.config.gateway.DictTypeGateway;
import com.lemnt.system.domain.config.model.DictType;
import com.lemnt.system.infrastructure.config.converter.DictTypeConverter;
import com.lemnt.system.infrastructure.config.dataobject.DictTypeDO;
import com.lemnt.system.infrastructure.config.mapper.DictTypeMapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * 字典类型网关实现 — infrastructure 层适配器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictTypeGatewayImpl implements DictTypeGateway {
    private final DictTypeMapper dictTypeMapper;
    private final RedisStringService redisStringService;

    @Override
    public DictType selectById(Long dictId) {
        return DictTypeConverter.toEntity(dictTypeMapper.selectById(dictId));
    }

    @Override
    public DictType selectByType(String dictType) {
        return DictTypeConverter.toEntity(dictTypeMapper.selectDictTypeByType(dictType));
    }

    @Override
    public PageResult<DictType> selectPage(DictType query, PageQry pageQry) {
        Page<DictTypeDO> page = new Page<>(pageQry.getPageNum(), pageQry.getPageSize());
        IPage<DictTypeDO> result = dictTypeMapper.selectPage(page, query);
        List<DictType> records = DictTypeConverter.toEntityList(result.getRecords());
        return PageResult.of(records, result.getTotal());
    }

    @Override
    public List<DictType> selectAll() {
        return DictTypeConverter.toEntityList(dictTypeMapper.selectDictTypeAll());
    }

    @Override
    public Boolean insert(DictType entity) {
        int row = dictTypeMapper.insert(DictTypeConverter.toDO(entity));
        return row > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(DictType entity) {
        return dictTypeMapper.updateById(DictTypeConverter.toDO(entity)) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(List<Long> dictIds) {
        for (Long dictId : dictIds) {
            DictTypeDO dictType = dictTypeMapper.selectById(dictId);
            if (dictType != null) {
                dictTypeMapper.deleteById(dictId);
                removeDictCache(dictType.getDictType());
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public DictType checkDictTypeUnique(String dictType) {
        return DictTypeConverter.toEntity(dictTypeMapper.checkDictTypeUnique(dictType));
    }

    /**
     * 删除指定字典缓存
     *
     * @param key 字典键
     */
    private void removeDictCache(String key) {
        redisStringService.delete(getCacheKey(key));
    }

    /**
     * 设置cache key
     *
     * @param configKey 参数键
     * @return 缓存键key
     */
    private static String getCacheKey(String configKey) {
        return CacheConstants.SYS_DICT_KEY + configKey;
    }
}
