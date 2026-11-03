package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.mall.domain.promotion.gateway.KefuMessageGateway;
import com.lemnt.mall.domain.promotion.model.KefuMessage;
import com.lemnt.mall.infrastructure.promotion.converter.KefuConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.KefuMessageDO;
import com.lemnt.mall.infrastructure.promotion.mapper.KefuMessageMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** 客服消息表查询与已读状态更新。 */
@Component
@RequiredArgsConstructor
public class KefuMessageGatewayImpl implements KefuMessageGateway {
    private final KefuMessageMapper mapper;

    @Override
    public KefuMessage save(KefuMessage value) {
        var row = KefuConverter.row(value);
        mapper.insert(row);
        return KefuConverter.model(row);
    }

    @Override
    public List<KefuMessage> list(Long conversationId, LocalDateTime beforeTime, int limit) {
        return mapper
                .selectList(
                        Wrappers.<KefuMessageDO>lambdaQuery()
                                .eq(KefuMessageDO::getConversationId, conversationId)
                                .lt(beforeTime != null, KefuMessageDO::getCreateTime, beforeTime)
                                .orderByDesc(KefuMessageDO::getCreateTime)
                                .orderByDesc(KefuMessageDO::getId)
                                .last("LIMIT " + limit))
                .stream()
                .map(KefuConverter::model)
                .toList();
    }

    @Override
    public boolean markRead(Long conversationId, int readerType) {
        mapper.update(
                null,
                Wrappers.<KefuMessageDO>lambdaUpdate()
                        .eq(KefuMessageDO::getConversationId, conversationId)
                        .ne(KefuMessageDO::getSenderType, readerType)
                        .eq(KefuMessageDO::getReadStatus, false)
                        .set(KefuMessageDO::getReadStatus, true));
        return true;
    }

    @Override
    public long unreadCount(Long conversationId, int readerType) {
        return mapper.selectCount(
                Wrappers.<KefuMessageDO>lambdaQuery()
                        .eq(KefuMessageDO::getConversationId, conversationId)
                        .ne(KefuMessageDO::getSenderType, readerType)
                        .eq(KefuMessageDO::getReadStatus, false));
    }
}
