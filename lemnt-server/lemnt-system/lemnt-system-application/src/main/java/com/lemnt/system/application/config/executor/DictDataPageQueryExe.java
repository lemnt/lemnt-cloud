package com.lemnt.system.application.config.executor;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.application.config.converter.DictDataConverter;
import com.lemnt.system.application.config.dto.DictDataDTO;
import com.lemnt.system.application.config.qry.DictDataPageQry;
import com.lemnt.system.domain.config.gateway.DictDataGateway;
import com.lemnt.system.domain.config.model.DictData;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 字典数据分页查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class DictDataPageQueryExe {
    private final DictDataGateway dictDataGateway;
    public PageResult<DictDataDTO> execute(DictDataPageQry qry) {
        DictData query = new DictData();
        query.setDictType(qry.dictType());
        query.setDictLabel(qry.dictLabel());
        query.setStatus(qry.status());
        PageResult<DictData> pageResult = dictDataGateway.selectPage(
                query, new PageQry(qry.pageNum(), qry.pageSize()));
        List<DictDataDTO> dtoList = DictDataConverter.toDTOList(pageResult.getRecords());
        return PageResult.of(dtoList, pageResult.getTotal());
    }
}
