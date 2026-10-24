package com.lemnt.system.application.notice.executor;

import com.lemnt.system.application.notice.converter.NoticeConverter;
import com.lemnt.system.domain.notice.model.Notice;
import com.lemnt.system.domain.notice.gateway.NoticeReadGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页顶部公告列表查询执行器（携带已读状态）
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class NoticeTopListQueryExe {
    private final NoticeReadGateway noticeReadGateway;

    /**
     * 执行查询顶部公告列表
     *
     * @param userId 当前用户ID
     * @param limit  最多条数
     * @return 公告列表（含已读标记）
     */
    public Map<String, Object> execute(Long userId, int limit) {
        List<Notice> list = noticeReadGateway.selectNoticeListWithReadStatus(userId, limit);
        Map<String, Object> result = new HashMap<>();
        result.put("list", NoticeConverter.toDTOList(list));
        result.put("unreadCount", noticeReadGateway.selectUnreadCount(userId));
        return result;
    }
}
