package com.lemnt.system.application.notice.cmd;

import java.io.Serializable;

/**
 * 修改通知公告命令对象
 *
 * @author wangxx
 */
public record NoticeUpdateCmd(
        /**
         * 公告ID
         */
        Long noticeId,
        /**
         * 公告标题
         */
        String noticeTitle,
        /**
         * 公告类型（1通知 2公告）
         */
        String noticeType,
        /**
         * 公告内容
         */
        String noticeContent,
        /**
         * 公告状态（1正常 2关闭）
         */
        String status,
        /**
         * 备注
         */
        String remark
) implements Serializable {
}
