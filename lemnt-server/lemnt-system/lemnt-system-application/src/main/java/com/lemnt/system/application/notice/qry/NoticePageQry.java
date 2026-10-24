package com.lemnt.system.application.notice.qry;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 通知公告分页查询条件对象
 *
 * @author wangxx
 */
public record NoticePageQry(
        /**
         * 公告标题
         */
        String noticeTitle,
        /**
         * 公告类型（1通知 2公告）
         */
        String noticeType,
        /**
         * 公告状态（1正常 2关闭）
         */
        String status,
        int pageNum,
        int pageSize,
        LocalDateTime beginTime,
        LocalDateTime endTime
) implements Serializable {
}
