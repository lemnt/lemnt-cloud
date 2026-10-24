package com.lemnt.system.application.notice.qry;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知公告分页查询条件对象
 *
 * @author wangxx
 */
public record NoticeReadUserPageQry(
        /**
         * 公告ID
         */
        Long noticeId,
        /**
         * 搜索关键词
         */
        String searchValue,
        int pageNum,
        int pageSize,
        LocalDateTime beginTime,
        LocalDateTime endTime
) implements Serializable {
}
