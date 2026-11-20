package com.lemnt.system.adapter.web.config.converter;

import com.lemnt.system.adapter.web.config.req.DictDataAddReq;
import com.lemnt.system.adapter.web.config.req.DictDataPageReq;
import com.lemnt.system.adapter.web.config.req.DictDataUpdateReq;
import com.lemnt.system.application.config.cmd.DictDataAddCmd;
import com.lemnt.system.application.config.cmd.DictDataUpdateCmd;
import com.lemnt.system.application.config.qry.DictDataPageQry;

/**
 * 字典数据请求对象转换器
 *
 * @author wangxx
 */
public class DictDataReqConverter {

    /**
     * HTTP参数转为应用查询，绑定和校验留在Adapter。
     */
    public static DictDataPageQry toQry(DictDataPageReq req) {
        return new DictDataPageQry(
            req.dictType(),
            req.dictLabel(),
            req.status(),
            req.pageNum(),
            req.pageSize(),
            req.beginTime(),
            req.endTime());
    }

    public static DictDataAddCmd toAddCmd(DictDataAddReq req) {
        return new DictDataAddCmd(
            req.dictSort(),
            req.dictLabel(),
            req.dictValue(),
            req.dictType(),
            req.cssClass(),
            req.listClass(),
            req.isDefault(),
            req.status(),
            req.remark());
    }

    public static DictDataUpdateCmd toUpdateCmd(DictDataUpdateReq req) {
        return new DictDataUpdateCmd(
            req.dictCode(),
            req.dictSort(),
            req.dictLabel(),
            req.dictValue(),
            req.dictType(),
            req.cssClass(),
            req.listClass(),
            req.isDefault(),
            req.status(),
            req.remark());
    }
}
