package com.lemnt.system.infrastructure.config.gateway;

import com.lemnt.common.core.constant.CacheConstants;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.redis.service.RedisStringService;
import com.lemnt.system.domain.config.gateway.DictDataGateway;
import com.lemnt.system.domain.config.model.DictData;
import com.lemnt.system.infrastructure.config.converter.DictDataConverter;
import com.lemnt.system.infrastructure.config.dataobject.DictDataDO;
import com.lemnt.system.infrastructure.config.mapper.DictDataMapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典数据网关实现 — infrastructure 层适配器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictDataGatewayImpl implements DictDataGateway {
    private final RedisStringService redisStringService;
    private final DictDataMapper dictDataMapper;

    @Override
    public DictData selectById(Long dictCode) {
        return DictDataConverter.toEntity(dictDataMapper.selectById(dictCode));
    }

    @Override
    public PageResult<DictData> selectPage(DictData query, PageQry pageQry) {
        Page<DictDataDO> page = new Page<>(pageQry.getPageNum(), pageQry.getPageSize());
        IPage<DictDataDO> result = dictDataMapper.selectPage(page, query);
        List<DictData> records = DictDataConverter.toEntityList(result.getRecords());
        return PageResult.of(records, result.getTotal());
    }

    @Override
    public List<DictData> selectByType(String dictType) {
        return DictDataConverter.toEntityList(dictDataMapper.selectDictDataByType(dictType));
    }

    @Override
    public Boolean insert(DictData entity) {
        return dictDataMapper.insert(DictDataConverter.toDO(entity)) > 0;
    }

    @Override
    public Boolean update(DictData entity) {
        return dictDataMapper.updateById(DictDataConverter.toDO(entity)) > 0;
    }

    @Override
    public Boolean deleteByIds(List<Long> dictCodes) {
        if (CollUtil.isEmpty(dictCodes)) {
            return false;
        }
        return dictDataMapper.deleteByIds(dictCodes) > 0;
    }

    @Override
    public Boolean deleteByType(String dictType) {
        return dictDataMapper.deleteDictDataByType(dictType) > 0;
    }

    @Override
    public Long countDictDataByType(String dictType) {
        return dictDataMapper.countDictDataByType(dictType);
    }
    /**
     * 项目启动时，初始化字典到缓存
     */
    @PostConstruct
    public void init() {
        resetCache();
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
    /** 修改字典项引用的类型名称。 */
    @Override
    public void renameType(String oldType, String newType) {
        dictDataMapper.updateDictDataType(oldType, newType);
    }

    /** 从数据库刷新单个字典的数据缓存。 */
    @Override
    public void refreshByType(String type) {
        setDictCache(type, DictDataConverter.toEntityList(dictDataMapper.selectDictDataByType(type)));
    }
}
