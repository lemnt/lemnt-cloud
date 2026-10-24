package com.lemnt.system.infrastructure.config.gateway;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.domain.config.model.DictData;
import com.lemnt.system.domain.config.gateway.DictDataGateway;
import com.lemnt.system.infrastructure.config.converter.DictDataConverter;
import com.lemnt.system.infrastructure.config.dataobject.DictDataDO;
import com.lemnt.system.infrastructure.config.mapper.DictDataMapper;

/**
 * 字典数据网关实现 — infrastructure 层适配器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictDataGatewayImpl implements DictDataGateway {

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
}
