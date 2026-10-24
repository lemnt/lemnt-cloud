package com.lemnt.system.application.config.qry;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 参数配置分页查询条件对象
 *
 * @author wangxx
 */
public record ConfigPageQry(
        /**
         * 参数名称
         */
        String configName,
        /**
         * 参数键名
         */
        String configKey,
        /**
         * 系统内置（1是 0否）
         */
        String configType,
        /**
         * 开始时间
         */
        LocalDateTime beginTime,
        /**
         * 结束时间
         */
        LocalDateTime endTime,
        int pageNum,
        int pageSize
) implements Serializable {
}
