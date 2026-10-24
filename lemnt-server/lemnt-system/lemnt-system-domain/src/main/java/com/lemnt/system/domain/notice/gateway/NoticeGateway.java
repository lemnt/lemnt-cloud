package com.lemnt.system.domain.notice.gateway;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.notice.model.Notice;

import java.util.List;

/**
 * 通知公告网关 — domain 层端口
 *
 * @author wangxx
 */
public interface NoticeGateway {

    /**
     * 查询公告信息
     *
     * @param noticeId 公告ID
     * @return 公告信息
     */
    Notice selectById(Long noticeId);

    /**
     * 分页查询公告列表
     *
     * @param query   查询条件
     * @param pageQry 分页参数
     * @return 分页结果
     */
    PageResult<Notice> selectPage(Notice query, PageQry pageQry);

    /**
     * 新增公告
     *
     * @param entity 公告信息
     * @return 是否成功
     */
    Boolean insert(Notice entity);

    /**
     * 修改公告
     *
     * @param entity 公告信息
     * @return 是否成功
     */
    Boolean update(Notice entity);

    /**
     * 批量删除公告
     *
     * @param noticeIds 公告ID数组
     * @return 是否成功
     */
    Boolean deleteByIds(List<Long> noticeIds);
}
