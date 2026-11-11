package com.lemnt.promotion.infrastructure.promotion.gateway;

import com.lemnt.promotion.domain.promotion.gateway.KefuConversationGateway;
import com.lemnt.promotion.domain.promotion.model.KefuConversation;
import com.lemnt.promotion.infrastructure.promotion.converter.KefuConverter;
import com.lemnt.promotion.infrastructure.promotion.dataobject.KefuConversationDO;
import com.lemnt.promotion.infrastructure.promotion.mapper.KefuConversationMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客服会话仅操作对应表，未读数使用原子自增。
 */
@Component
@RequiredArgsConstructor
public class KefuConversationGatewayImpl implements KefuConversationGateway {
    private final KefuConversationMapper mapper;

    @Override
    public KefuConversation findByUserId(Long userId) {
        return KefuConverter.model(
            mapper.selectOne(
                Wrappers.<KefuConversationDO>lambdaQuery()
                    .eq(KefuConversationDO::getUserId, userId)
                    .last("LIMIT 1")));
    }

    @Override
    public KefuConversation find(Long id) {
        return KefuConverter.model(mapper.selectById(id));
    }

    @Override
    public KefuConversation save(KefuConversation value) {
        var row = KefuConverter.row(value);
        if (row.getId() == null) {
            mapper.insert(row);
        } else {
            mapper.updateById(row);
        }
        return KefuConverter.model(row);
    }

    @Override
    public List<KefuConversation> listVisible() {
        return mapper
            .selectList(
                Wrappers.<KefuConversationDO>lambdaQuery()
                    .eq(KefuConversationDO::getAdminDeleted, false)
                    .orderByDesc(KefuConversationDO::getAdminPinned)
                    .orderByDesc(KefuConversationDO::getLastMessageTime))
            .stream()
            .map(KefuConverter::model)
            .toList();
    }

    @Override
    public boolean updateLastMessage(
        Long id, LocalDateTime time, String content, Integer contentType, boolean fromMember) {
        int updated =
            mapper.update(
                null,
                Wrappers.<KefuConversationDO>lambdaUpdate()
                    .eq(KefuConversationDO::getId, id)
                    .set(KefuConversationDO::getLastMessageTime, time)
                    .set(KefuConversationDO::getLastMessageContent, content)
                    .set(KefuConversationDO::getLastMessageContentType, contentType)
                    .set(KefuConversationDO::getAdminDeleted, false));
        if (updated != 1) {
            return false;
        }
        if (fromMember) {
            mapper.update(
                null,
                Wrappers.<KefuConversationDO>lambdaUpdate()
                    .eq(KefuConversationDO::getId, id)
                    .setIncrBy(KefuConversationDO::getAdminUnreadMessageCount, 1));
        }
        return true;
    }

    @Override
    public boolean clearAdminUnread(Long id) {
        return mapper.update(
            null,
            Wrappers.<KefuConversationDO>lambdaUpdate()
                .eq(KefuConversationDO::getId, id)
                .set(KefuConversationDO::getAdminUnreadMessageCount, 0))
            == 1;
    }

    @Override
    public boolean setPinned(Long id, boolean pinned) {
        return mapper.update(
            null,
            Wrappers.<KefuConversationDO>lambdaUpdate()
                .eq(KefuConversationDO::getId, id)
                .set(KefuConversationDO::getAdminPinned, pinned))
            == 1;
    }

    @Override
    public boolean setAdminDeleted(Long id, boolean deleted) {
        return mapper.update(
            null,
            Wrappers.<KefuConversationDO>lambdaUpdate()
                .eq(KefuConversationDO::getId, id)
                .set(KefuConversationDO::getAdminDeleted, deleted))
            == 1;
    }
}
