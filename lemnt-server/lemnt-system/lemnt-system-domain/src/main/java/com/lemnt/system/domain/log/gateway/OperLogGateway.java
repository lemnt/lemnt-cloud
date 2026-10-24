package com.lemnt.system.domain.log.gateway;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.log.model.OperLog;

import java.util.List;

/**
 * 操作日志网关 — domain 层端口
 *
 * @author wangxx
 */
public interface OperLogGateway {

    /**
     * 分页查询操作日志
     *
     * @param query   查询条件
     * @param pageQry 分页参数
     * @return 分页结果
     */
    PageResult<OperLog> selectPage(OperLog query, PageQry pageQry);

    /**
     * 新增操作日志
     *
     * @param entity 操作日志信息
     * @return 是否成功
     */
    Boolean insert(OperLog entity);

    /**
     * 批量删除操作日志
     *
     * @param operIds 日志ID数组
     * @return 是否成功
     */
    Boolean deleteByIds(List<Long> operIds);

    /**
     * 查询操作日志详细
     *
     * @param operId 操作日志ID
     * @return 操作日志信息
     */
    OperLog selectById(Long operId);

    /**
     * 清空操作日志
     */
    void clean();
}
