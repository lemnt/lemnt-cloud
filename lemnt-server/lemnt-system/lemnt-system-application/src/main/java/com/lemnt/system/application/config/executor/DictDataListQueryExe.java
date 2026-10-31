package com.lemnt.system.application.config.executor;

import com.lemnt.system.application.config.converter.DictDataConverter;
import com.lemnt.system.application.config.dto.DictDataDTO;
import com.lemnt.system.domain.config.gateway.DictDataGateway;
import com.lemnt.system.domain.config.model.DictData;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 字典数据查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictDataListQueryExe {
    private final DictDataGateway dictDataGateway;
    public List<DictDataDTO> execute(String dictType) {
        List<DictData> dictDataList = dictDataGateway.selectByType(dictType);
        return DictDataConverter.toDTOList(dictDataList);
    }
}
