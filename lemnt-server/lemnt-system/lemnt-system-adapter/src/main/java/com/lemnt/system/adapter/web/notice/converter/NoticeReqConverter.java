package com.lemnt.system.adapter.web.notice.converter;

import com.lemnt.system.adapter.web.notice.req.NoticeAddReq;
import com.lemnt.system.adapter.web.notice.req.NoticePageReq;
import com.lemnt.system.adapter.web.notice.req.NoticeReadUsersPageReq;
import com.lemnt.system.adapter.web.notice.req.NoticeUpdateReq;
import com.lemnt.system.application.notice.cmd.NoticeAddCmd;
import com.lemnt.system.application.notice.cmd.NoticeUpdateCmd;
import com.lemnt.system.application.notice.qry.NoticePageQry;
import com.lemnt.system.application.notice.qry.NoticeReadUserPageQry;

/**
 * 通知公告请求对象转换器
 *
 * @author wangxx
 */
public class NoticeReqConverter {

    public static NoticeAddCmd toCmd(NoticeAddReq req) {
        return new NoticeAddCmd(
                req.noticeTitle(),
                req.noticeType(),
                req.noticeContent(),
                req.status(),
                req.remark()
        );
    }

    public static NoticeUpdateCmd toCmd(NoticeUpdateReq req) {
        return new NoticeUpdateCmd(
                req.noticeId(),
                req.noticeTitle(),
                req.noticeType(),
                req.noticeContent(),
                req.status(),
                req.remark()
        );
    }

    public static NoticePageQry toQry(NoticePageReq item) {
        if (item == null) {
            return null;
        }
        return new NoticePageQry(
                item.noticeTitle(),
                item.noticeType(),
                item.status(),
                item.pageNum(),
                item.pageSize(),
                item.beginTime(),
                item.endTime()
        );
    }

    public static NoticeReadUserPageQry toQry(NoticeReadUsersPageReq item) {
        if (item == null) {
            return null;
        }
        return new NoticeReadUserPageQry(
                item.noticeId(),
                item.searchValue(),
                item.pageNum(),
                item.pageSize(),
                item.beginTime(),
                item.endTime()
        );
    }
}
