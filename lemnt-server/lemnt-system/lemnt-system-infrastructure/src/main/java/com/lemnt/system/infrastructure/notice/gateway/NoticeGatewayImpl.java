package com.lemnt.system.infrastructure.notice.gateway;

import com.lemnt.common.domain.model.PageQry;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.system.domain.notice.gateway.NoticeGateway;
import com.lemnt.system.domain.notice.model.Notice;
import com.lemnt.system.infrastructure.notice.converter.NoticeConverter;
import com.lemnt.system.infrastructure.notice.dataobject.NoticeDO;
import com.lemnt.system.infrastructure.notice.mapper.NoticeMapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Component;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 通知公告网关实现 — infrastructure 层适配器
 *
 * @author wangxx
 */
@Component
@RequiredArgsConstructor
public class NoticeGatewayImpl implements NoticeGateway {
    private final NoticeMapper noticeMapper;

    @Override
    public Notice selectById(Long noticeId) {
        return NoticeConverter.toEntity(noticeMapper.selectById(noticeId));
    }

    @Override
    public PageResult<Notice> selectPage(Notice query, PageQry pageQry) {
        Page<NoticeDO> page = new Page<>(pageQry.getPageNum(), pageQry.getPageSize());
        IPage<NoticeDO> result = noticeMapper.selectPage(page, query);
        List<Notice> records = NoticeConverter.toEntityList(result.getRecords());
        return PageResult.of(records, result.getTotal());
    }

    @Override
    public Boolean insert(Notice entity) {
        return noticeMapper.insert(NoticeConverter.toDO(entity)) > 0;
    }

    @Override
    public Boolean update(Notice entity) {
        return noticeMapper.updateById(NoticeConverter.toDO(entity)) > 0;
    }

    @Override
    public Boolean deleteByIds(List<Long> noticeIds) {
        if (CollUtil.isEmpty(noticeIds)) {
            return true;
        }
        return noticeMapper.deleteByIds(noticeIds) > 0;
    }
}
