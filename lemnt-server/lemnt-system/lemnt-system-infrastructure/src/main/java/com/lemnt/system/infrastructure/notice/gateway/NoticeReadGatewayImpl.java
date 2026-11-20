package com.lemnt.system.infrastructure.notice.gateway;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.notice.gateway.NoticeReadGateway;
import com.lemnt.system.domain.notice.model.Notice;
import com.lemnt.system.domain.notice.model.NoticeRead;
import com.lemnt.system.domain.notice.model.NoticeReadUser;
import com.lemnt.system.infrastructure.notice.dataobject.NoticeDO;
import com.lemnt.system.infrastructure.notice.dataobject.NoticeReadDO;
import com.lemnt.system.infrastructure.notice.mapper.NoticeReadMapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 公告已读网关实现 — infrastructure 层适配器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class NoticeReadGatewayImpl implements NoticeReadGateway {
    private final NoticeReadMapper noticeReadMapper;

    @Override
    public Boolean insert(NoticeRead entity) {
        NoticeReadDO data = new NoticeReadDO();
        data.setNoticeId(entity.getNoticeId());
        data.setUserId(entity.getUserId());
        data.setReadTime(entity.getReadTime());
        noticeReadMapper.insertReadRecords(List.of(data));
        return true;
    }

    @Override
    public PageResult<NoticeReadUser> selectPageReadUsers(
        Long noticeId, String searchValue, PageQry qry) {
        Page<NoticeDO> page = new Page<>(qry.getPageNum(), qry.getPageSize());
        IPage<NoticeReadUser> result =
            noticeReadMapper.selectPageReadUsers(page, noticeId, searchValue);
        return PageResult.of(result.getRecords(), result.getTotal());
    }

    @Override
    public List<Notice> selectNoticeListWithReadStatus(Long userId, int limit) {
        return noticeReadMapper.selectNoticeListWithReadStatus(userId, limit);
    }

    @Override
    public int selectUnreadCount(Long userId) {
        return noticeReadMapper.selectUnreadCount(userId);
    }

    @Override
    public Boolean deleteByNoticeIds(List<Long> noticeIds) {
        return deleteByNoticeIdsRow(noticeIds) > 0;
    }

    @Override
    public Boolean isRead(Long noticeId, Long userId) {
        return selectIsReadRow(noticeId, userId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean markReadBatch(Long userId, List<Long> noticeIds) {
        if (CollUtil.isEmpty(noticeIds)) {
            return true;
        }
        List<NoticeReadDO> list = new ArrayList<>();
        for (Long noticeId : noticeIds) {
            NoticeReadDO data = new NoticeReadDO();
            data.setNoticeId(noticeId);
            data.setUserId(userId);
            data.setReadTime(LocalDateTime.now());
            list.add(data);
        }
        noticeReadMapper.insertReadRecords(list);
        return true;
    }

    /**
     * 查询某用户是否已读某公告
     *
     * @param noticeId 公告ID
     * @param userId   用户ID
     * @return 已读记录数（0未读 1已读）
     */
    private long selectIsReadRow(Long noticeId, Long userId) {
        return noticeReadMapper.selectCount(
            Wrappers.<NoticeReadDO>lambdaQuery()
                .eq(NoticeReadDO::getNoticeId, noticeId)
                .eq(NoticeReadDO::getUserId, userId));
    }

    /**
     * 公告删除时清理对应已读记录
     *
     * @param noticeIds 公告ID数组
     * @return 结果
     */
    private int deleteByNoticeIdsRow(List<Long> noticeIds) {
        if (CollUtil.isEmpty(noticeIds)) {
            return 0;
        }
        return noticeReadMapper.delete(
            Wrappers.<NoticeReadDO>lambdaQuery().in(NoticeReadDO::getNoticeId, noticeIds));
    }
}
