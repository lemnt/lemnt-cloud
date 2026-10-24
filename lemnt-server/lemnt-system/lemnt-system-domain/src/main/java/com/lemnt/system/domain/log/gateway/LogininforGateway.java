package com.lemnt.system.domain.log.gateway;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.log.model.Logininfor;

import java.util.List;

/**
 * 登录日志网关 — domain 层端口
 *
 * @author wangxx
 */
public interface LogininforGateway {

    /**
     * 分页查询登录日志
     *
     * @param query   查询条件
     * @param pageQry 分页参数
     * @return 分页结果
     */
    PageResult<Logininfor> selectPage(Logininfor query, PageQry pageQry);

    /**
     * 新增登录日志
     *
     * @param entity 登录日志信息
     * @return 是否成功
     */
    Boolean insert(Logininfor entity);

    /**
     * 批量删除登录日志
     *
     * @param infoIds 日志ID数组
     * @return 是否成功
     */
    Boolean deleteByIds(List<Long> infoIds);

    /**
     * 清空登录日志
     */
    void clean();
}
