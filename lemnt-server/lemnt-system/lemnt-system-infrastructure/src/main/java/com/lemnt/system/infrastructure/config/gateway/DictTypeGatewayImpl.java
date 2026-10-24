package com.lemnt.system.infrastructure.config.gateway;

import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.collection.CollUtil;
import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.redis.service.RedisStringService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.lemnt.system.domain.config.model.DictData;
import com.lemnt.system.domain.config.model.DictType;
import com.lemnt.system.domain.config.gateway.DictTypeGateway;
import com.lemnt.system.infrastructure.config.converter.DictDataConverter;
import com.lemnt.system.infrastructure.config.converter.DictTypeConverter;
import com.lemnt.system.infrastructure.config.dataobject.DictDataDO;
import com.lemnt.system.infrastructure.config.dataobject.DictTypeDO;
import com.lemnt.system.infrastructure.config.mapper.DictDataMapper;
import com.lemnt.system.infrastructure.config.mapper.DictTypeMapper;

/**
 * 字典类型网关实现 — infrastructure 层适配器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictTypeGatewayImpl implements DictTypeGateway {

    private final DictTypeMapper dictTypeMapper;
    private final DictDataMapper dictDataMapper;
    private final RedisStringService redisStringService;

    /**
     * 项目启动时，初始化字典到缓存
     */
    @PostConstruct
    public void init() {
        resetCache();
    }

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
        if (row > 0) {
            setDictCache(entity.getDictType(), null);
        }
        return row > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(DictType entity) {
        DictTypeDO old = dictTypeMapper.selectById(entity.getDictId());
        if (old != null) {
            dictDataMapper.updateDictDataType(old.getDictType(), entity.getDictType());
        }
        int row = dictTypeMapper.updateById(DictTypeConverter.toDO(entity));
        if (row > 0) {
            List<DictDataDO> dictDatas = dictDataMapper.selectDictDataByType(entity.getDictType());
            setDictCache(entity.getDictType(), DictDataConverter.toEntityList(dictDatas));
        }
        return row > 0;
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
    public void clearCache() {
        clearDictCache();
    }

    @Override
    public void resetCache() {
        clearCache();
        List<DictDataDO> query = dictDataMapper.selectList(null);
        Map<String, List<DictDataDO>> dictDataMap = query
                .stream().collect(Collectors.groupingBy(DictDataDO::getDictType));
        for (Map.Entry<String, List<DictDataDO>> entry : dictDataMap.entrySet()) {
            List<DictDataDO> sorted = entry.getValue().stream()
                    .sorted(Comparator.comparing(DictDataDO::getDictSort))
                    .collect(Collectors.toList());
            setDictCache(entry.getKey(), DictDataConverter.toEntityList(sorted));
        }
    }

    @Override
    public void clearCacheByType(String dictType) {
        removeDictCache(dictType);
    }

    @Override
    public DictType checkDictTypeUnique(String dictType) {
        return DictTypeConverter.toEntity(dictTypeMapper.checkDictTypeUnique(dictType));
    }

    @Override
    public List<DictData> selectDictDataByType(String dictType) {
        List<DictData> cached = getDictCache(dictType);
        if (CollUtil.isNotEmpty(cached)) {
            return cached;
        }
        List<DictDataDO> list = dictDataMapper.selectDictDataByType(dictType);
        if (list != null && !list.isEmpty()) {
            setDictCache(dictType, DictDataConverter.toEntityList(list));
        }
        return DictDataConverter.toEntityList(list);
    }

    /**
     * 设置字典缓存
     *
     * @param key          参数键
     * @param dictDataList 字典数据列表
     */
    private void setDictCache(String key, List<DictData> dictDataList) {
        if (CollectionUtils.isEmpty(dictDataList)) {
            return;
        }
        redisStringService.set(getCacheKey(key), JsonUtils.toJsonStr(dictDataList));
    }

    /**
     * 获取字典缓存
     *
     * @param key 参数键
     * @return dictDatas 字典数据列表
     */
    private List<DictData> getDictCache(String key) {
        String cacheValue = redisStringService.get(getCacheKey(key));
        if (StringUtils.isNotBlank(cacheValue)) {
            return JsonUtils.toList(cacheValue, DictData.class);
        }
        return new ArrayList<>();
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
     * 清空字典缓存
     */
    private void clearDictCache() {
        Set<String> keys = redisStringService.keys(CacheConstants.SYS_DICT_KEY + "*");
        for (String key : keys) {
            redisStringService.delete(key);
        }
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
