package com.lemnt.system.application.config.executor;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.application.config.converter.DictTypeConverter;
import com.lemnt.system.application.config.dto.DictTypeDTO;
import com.lemnt.system.application.config.qry.DictTypePageQry;
import com.lemnt.system.domain.config.gateway.DictTypeGateway;
import com.lemnt.system.domain.config.model.DictType;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 字典类型分页查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictTypePageQueryExe {
    private final DictTypeGateway dictTypeGateway;
    public PageResult<DictTypeDTO> execute(DictTypePageQry qry) {
        DictType query = new DictType();
        query.setDictName(qry.dictName());
        query.setDictType(qry.dictType());
        query.setStatus(qry.status());
        PageResult<DictType> pageResult = dictTypeGateway.selectPage(
                query, new PageQry(qry.pageNum(), qry.pageSize()));
        List<DictTypeDTO> dtoList = DictTypeConverter.toDTOList(pageResult.getRecords());
        return PageResult.of(dtoList, pageResult.getTotal());
    }
}
