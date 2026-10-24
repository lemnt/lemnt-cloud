package com.lemnt.system.application.org.qry;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位分页查询条件对象
 *
 * @author wangxx
 */
public record PostPageQry(
        /**
         * 岗位编码
         */
        String postCode,
        /**
         * 岗位名称
         */
        String postName,
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
