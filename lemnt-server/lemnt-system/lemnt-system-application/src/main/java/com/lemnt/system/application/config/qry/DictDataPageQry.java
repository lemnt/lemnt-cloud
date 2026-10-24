package com.lemnt.system.application.config.qry;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典数据分页查询条件对象
 *
 * @author wangxx
 */
public record DictDataPageQry(
        /**
         * 字典类型
         */
        String dictType,
        /**
         * 字典标签
         */
        String dictLabel,
        /**
         * 状态（1正常 2停用）
         */
        String status,
        int pageNum,
        int pageSize,
        LocalDateTime beginTime,
        LocalDateTime endTime
) implements Serializable {
}
