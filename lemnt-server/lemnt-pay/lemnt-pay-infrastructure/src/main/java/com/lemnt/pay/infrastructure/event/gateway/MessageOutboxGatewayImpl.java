package com.lemnt.pay.infrastructure.event.gateway;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.pay.domain.event.gateway.MessageOutboxGateway;
import com.lemnt.pay.domain.event.model.OutboxMessage;
import com.lemnt.pay.infrastructure.event.dataobject.MessageOutboxDO;
import com.lemnt.pay.infrastructure.event.mapper.MessageOutboxMapper;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** 与业务同库登记，投递租约采用独立短事务。 */
@Component
@RequiredArgsConstructor
public class MessageOutboxGatewayImpl implements MessageOutboxGateway {
    private final MessageOutboxMapper mapper;

    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    public void enqueue(String type, String key, String aggregate, String payload) {
        if (!Set.of(CommerceEventTopics.PAY_SUCCESS, CommerceEventTopics.PAY_REFUND_SUCCESS)
                        .contains(type)
                || key == null
                || key.isBlank()
                || aggregate == null
                || aggregate.isBlank()
                || payload == null
                || payload.isBlank()) {
            throw new IllegalArgumentException("事件内容不完整或类型未知");
        }
        String hash = hash(payload);
        String eventNo = UUID.randomUUID().toString();
        var envelope =
                new CommerceEventEnvelope(
                        eventNo,
                        type,
                        1,
                        key,
                        aggregate,
                        Instant.now(),
                        MDC.get("traceId"),
                        payload);
        mapper.insertIfAbsent(
                new MessageOutboxDO()
                        .setEventNo(eventNo)
                        .setEventType(type)
                        .setBusinessKey(key)
                        .setAggregateNo(aggregate)
                        .setSchemaVersion(1)
                        .setPayload(JsonUtils.toJsonStr(envelope))
                        .setPayloadHash(hash));
        var previous = mapper.lockBusiness(type, key);
        if (previous == null
                || !aggregate.equals(previous.getAggregateNo())
                || !hash.equals(previous.getPayloadHash())
                || previous.getSchemaVersion() != 1) {
            throw new IllegalStateException("同事件业务编号的冻结内容不一致");
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public List<OutboxMessage> claim(int size) {
        if (size < 1 || size > 100) throw new IllegalArgumentException("批量领取范围为1到100");
        return mapper.lockDue(size).stream()
                .filter(row -> mapper.claim(row.getId(), row.getExecuteVersion()) == 1)
                .map(
                        row ->
                                new OutboxMessage(
                                        row.getId(),
                                        row.getEventNo(),
                                        row.getEventType(),
                                        row.getBusinessKey(),
                                        row.getPayload(),
                                        row.getAttempts() + 1,
                                        row.getExecuteVersion() + 1))
                .toList();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean sent(OutboxMessage message) {
        return mapper.sent(message.id(), message.executeVersion()) == 1;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void failed(OutboxMessage message, String error) {
        int delay = Math.min(600, 5 * (1 << Math.min(message.attempts() - 1, 7)));
        String summary = error == null ? "投递失败" : error.substring(0, Math.min(500, error.length()));
        mapper.failed(
                message.id(),
                message.executeVersion(),
                message.attempts() >= 20 ? "FAILED" : "PENDING",
                delay,
                summary);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean retryFailed(String eventNo) {
        return mapper.retryFailed(eventNo) == 1;
    }

    private static String hash(String payload) {
        try {
            return HexFormat.of()
                    .formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256不可用", impossible);
        }
    }
}
