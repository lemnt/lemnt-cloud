package com.lemnt.system.application.config.executor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.lemnt.system.application.config.converter.DictDataConverter;
import com.lemnt.system.application.config.dto.DictDataDTO;
import com.lemnt.system.domain.config.gateway.DictDataGateway;

/**
 * 字典数据详情查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictDataDetailQueryExe {
    private final DictDataGateway dictDataGateway;

    public DictDataDTO execute(Long dictCode) {
        return DictDataConverter.toDTO(dictDataGateway.selectById(dictCode));
    }
}
