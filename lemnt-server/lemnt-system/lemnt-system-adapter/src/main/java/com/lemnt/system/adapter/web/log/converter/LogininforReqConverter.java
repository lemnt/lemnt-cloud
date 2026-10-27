package com.lemnt.system.adapter.web.log.converter;

import com.lemnt.system.adapter.web.log.req.LogininforPageReq;
import com.lemnt.system.application.log.qry.LogininforPageQry;

/**
 * 登录日志响应对象组装器
 *
 * @author wangxx
 */
public class LogininforReqConverter {
    public static LogininforPageQry toQry(LogininforPageReq item) {
        if (item == null) {
            return null;
        }
        return new LogininforPageQry(
                item.userName(),
                item.ipaddr(),
                item.status(),
                item.pageNum(),
                item.pageSize(),
                item.beginTime(),
                item.endTime()
        );
    }
}
