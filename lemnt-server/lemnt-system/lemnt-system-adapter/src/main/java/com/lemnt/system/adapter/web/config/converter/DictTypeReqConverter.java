package com.lemnt.system.adapter.web.config.converter;

import com.lemnt.system.adapter.web.config.req.DictTypeAddReq;
import com.lemnt.system.adapter.web.config.req.DictTypePageReq;
import com.lemnt.system.adapter.web.config.req.DictTypeUpdateReq;
import com.lemnt.system.application.config.cmd.DictTypeAddCmd;
import com.lemnt.system.application.config.cmd.DictTypeUpdateCmd;
import com.lemnt.system.application.config.qry.DictTypePageQry;

/**
 * 字典类型请求对象转换器
 *
 * @author wangxx
 */
public class DictTypeReqConverter {

    public static DictTypeAddCmd toCmd(DictTypeAddReq req) {
        return new DictTypeAddCmd(
                req.dictName(),
                req.dictType(),
                req.status(),
                req.remark()
        );
    }

    public static DictTypeUpdateCmd toCmd(DictTypeUpdateReq req) {
        return new DictTypeUpdateCmd(
                req.dictId(),
                req.dictName(),
                req.dictType(),
                req.status(),
                req.remark()
        );
    }

    public static DictTypePageQry toQry(DictTypePageReq item) {
        if (item == null) {
            return null;
        }
        return new DictTypePageQry(
                item.dictName(),
                item.dictType(),
                item.status(),
                item.pageNum(),
                item.pageSize(),
                item.beginTime(),
                item.endTime()
        );
    }
}
