package com.lemnt.system.application.log.executor;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.application.log.converter.LogininforConverter;
import com.lemnt.system.application.log.dto.LogininforDTO;
import com.lemnt.system.application.log.qry.LogininforPageQry;
import com.lemnt.system.domain.log.gateway.LogininforGateway;
import com.lemnt.system.domain.log.model.Logininfor;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 登录日志列表查询执行器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class LogininforPageQueryExe {
    private final LogininforGateway logininforGateway;

    /**
     * 执行登录日志分页查询
     *
     * @param qry 查询条件
     * @return 分页结果
     */
    public PageResult<LogininforDTO> execute(LogininforPageQry qry) {
        Logininfor query = new Logininfor();
        query.setUserName(qry.userName());
        query.setIpaddr(qry.ipaddr());
        query.setStatus(qry.status());
        query.setBeginTime(qry.beginTime());
        query.setEndTime(qry.endTime());
        PageQry pageQuery = new PageQry(qry.pageNum(), qry.pageSize());
        PageResult<Logininfor> pageResult = logininforGateway.selectPage(query, pageQuery);
        List<LogininforDTO> dtoList = LogininforConverter.toDTOList(pageResult.getRecords());
        return PageResult.of(dtoList, pageResult.getTotal());
    }
}
