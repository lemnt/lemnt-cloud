package com.lemnt.system.adapter.web.config.converter;

import com.lemnt.system.adapter.web.config.req.ConfigAddReq;
import com.lemnt.system.adapter.web.config.req.ConfigPageReq;
import com.lemnt.system.adapter.web.config.req.ConfigUpdateReq;
import com.lemnt.system.application.config.cmd.ConfigAddCmd;
import com.lemnt.system.application.config.cmd.ConfigUpdateCmd;
import com.lemnt.system.application.config.qry.ConfigPageQry;

/**
 * 参数配置请求对象转换器
 *
 * @author wangxx
 */
public class ConfigReqConverter {

    public static ConfigAddCmd toAddCmd(ConfigAddReq req) {
        return new ConfigAddCmd(
                req.configName(),
                req.configKey(),
                req.configValue(),
                req.configType(),
                req.remark()
        );
    }

    public static ConfigUpdateCmd toUpdateCmd(ConfigUpdateReq req) {
        return new ConfigUpdateCmd(
                req.configId(),
                req.configName(),
                req.configKey(),
                req.configValue(),
                req.configType(),
                req.remark()
        );
    }

    public static ConfigPageQry toQry(ConfigPageReq item) {
        if (item == null) {
            return null;
        }
        return new ConfigPageQry(
                item.configName(),
                item.configKey(),
                item.configType(),
                item.beginTime(),
                item.endTime(),
                item.pageNum(),
                item.pageSize()
        );
    }
}
