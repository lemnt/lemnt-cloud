package com.lemnt.system.adapter.web.log.converter;

import com.lemnt.system.adapter.web.log.req.OperLogPageReq;
import com.lemnt.system.application.log.qry.OperLogPageQry;

/**
 * 操作日志响应对象组装器
 *
 * @author wangxx
 */
public class OperLogReqConverter {

    public static OperLogPageQry toQry(OperLogPageReq item) {
        if (item == null) {
            return null;
        }
        return new OperLogPageQry(
                item.title(),
                item.businessType(),
                item.businessTypes(),
                item.status(),
                item.operName(),
                item.pageNum(),
                item.pageSize(),
                item.beginTime(),
                item.endTime()
        );
    }
}
