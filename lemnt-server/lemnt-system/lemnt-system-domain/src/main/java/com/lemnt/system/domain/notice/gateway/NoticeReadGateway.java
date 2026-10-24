package com.lemnt.system.domain.notice.gateway;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.notice.model.Notice;
import com.lemnt.system.domain.notice.model.NoticeRead;
import com.lemnt.system.domain.notice.model.NoticeReadUser;

import java.util.List;

/**
 * 公告已读网关 — domain 层端口
 *
 * @author wangxx
 */
public interface NoticeReadGateway {

    /**
     * 新增已读记录
     *
     * @param entity 已读记录信息
     * @return 是否成功
     */
    Boolean insert(NoticeRead entity);

    /**
     * 查询某公告的已读用户列表
     *
     * @param noticeId    公告ID
     * @param searchValue 搜索关键字
     * @return 已读用户列表
     */
    PageResult<NoticeReadUser> selectPageReadUsers(Long noticeId, String searchValue, PageQry qry);

    /**
     * 查询公告列表（携带当前用户已读状态）
     *
     * @param userId 用户ID
     * @param limit  查询数量
     * @return 公告列表
     */
    List<Notice> selectNoticeListWithReadStatus(Long userId, int limit);

    /**
     * 查询当前用户所有正常状态公告的未读数量，不受顶部列表条数限制。
     */
    int selectUnreadCount(Long userId);

    /**
     * 根据公告ID列表删除已读记录
     *
     * @param noticeIds 公告ID数组
     * @return 是否成功
     */
    Boolean deleteByNoticeIds(List<Long> noticeIds);

    /**
     * 批量标记已读
     *
     * @param userId    用户ID
     * @param noticeIds 公告ID数组
     * @return 结果
     */
    Boolean markReadBatch(Long userId, List<Long> noticeIds);

    /**
     * 查询用户是否已读某公告
     *
     * @param noticeId 公告ID
     * @param userId   用户ID
     * @return 是否已读
     */
    Boolean isRead(Long noticeId, Long userId);
}
