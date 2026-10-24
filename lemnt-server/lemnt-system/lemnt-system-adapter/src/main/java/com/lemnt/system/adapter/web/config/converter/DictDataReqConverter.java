package com.lemnt.system.adapter.web.config.converter;

import com.lemnt.system.adapter.web.config.req.DictDataAddReq;
import com.lemnt.system.adapter.web.config.req.DictDataUpdateReq;
import com.lemnt.system.application.config.cmd.DictDataAddCmd;
import com.lemnt.system.application.config.cmd.DictDataUpdateCmd;

/**
 * 字典数据请求对象转换器
 *
 * @author wangxx
 */
public class DictDataReqConverter {

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
                req.remark()
        );
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
                req.remark()
        );
    }
}
