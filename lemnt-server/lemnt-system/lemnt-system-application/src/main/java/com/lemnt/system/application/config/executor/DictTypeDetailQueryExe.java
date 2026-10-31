package com.lemnt.system.application.config.executor;

import com.lemnt.system.application.config.converter.DictDataConverter;
import com.lemnt.system.application.config.converter.DictTypeConverter;
import com.lemnt.system.application.config.dto.DictDataDTO;
import com.lemnt.system.application.config.dto.DictTypeDTO;
import com.lemnt.system.domain.config.gateway.DictDataGateway;
import com.lemnt.system.domain.config.gateway.DictTypeGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 字典类型详情查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictTypeDetailQueryExe {
    private final DictDataGateway dictDataGateway;
    private final DictTypeGateway dictTypeGateway;
    public DictTypeDTO executeById(Long dictId) {
        return DictTypeConverter.toDTO(dictTypeGateway.selectById(dictId));
    }

    public DictTypeDTO executeByType(String dictType) {
        return DictTypeConverter.toDTO(dictTypeGateway.selectByType(dictType));
    }

    public List<DictDataDTO> executeDictDataByType(String dictType) {
        return DictDataConverter
                .toDTOList(dictDataGateway.selectDictDataByType(dictType));
    }
}
