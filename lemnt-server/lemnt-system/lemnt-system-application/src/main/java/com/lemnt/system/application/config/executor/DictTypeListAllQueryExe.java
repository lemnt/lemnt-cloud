package com.lemnt.system.application.config.executor;

import com.lemnt.system.application.config.converter.DictTypeConverter;
import com.lemnt.system.application.config.dto.DictTypeDTO;
import com.lemnt.system.domain.config.gateway.DictTypeGateway;
import com.lemnt.system.domain.config.model.DictType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 字典类型数据查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictTypeListAllQueryExe {
    private final DictTypeGateway dictTypeGateway;

    public List<DictTypeDTO> execute() {
        List<DictType> dictTypeList = dictTypeGateway.selectAll();
        return DictTypeConverter.toDTOList(dictTypeList);
    }
}
