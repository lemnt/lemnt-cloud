package com.lemnt.system.application.log.executor;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.application.log.converter.OperLogConverter;
import com.lemnt.system.application.log.dto.OperLogDTO;
import com.lemnt.system.application.log.qry.OperLogPageQry;
import com.lemnt.system.domain.log.gateway.OperLogGateway;
import com.lemnt.system.domain.log.model.OperLog;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 操作日志分页查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class OperLogPageQueryExe {
    private final OperLogGateway operLogGateway;

    /**
     * 执行操作日志分页查询
     *
     * @param qry 查询条件
     * @return 分页结果
     */
    public PageResult<OperLogDTO> execute(OperLogPageQry qry) {
        OperLog query = new OperLog();
        query.setTitle(qry.title());
        query.setBusinessType(qry.businessType());
        query.setBusinessTypes(qry.businessTypes());
        query.setStatus(qry.status());
        query.setOperName(qry.operName());
        query.setBeginTime(qry.beginTime());
        query.setEndTime(qry.endTime());
        PageQry pageQry = new PageQry(qry.pageNum(), qry.pageSize());
        PageResult<OperLog> pageResult = operLogGateway.selectPage(query, pageQry);
        List<OperLogDTO> dtoList = OperLogConverter.toDTOList(pageResult.getRecords());
        return PageResult.of(dtoList, pageResult.getTotal());
    }
}
