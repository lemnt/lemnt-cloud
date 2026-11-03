package com.lemnt.pay.infrastructure.event.gateway;

import com.lemnt.pay.domain.event.gateway.MessageInboxGateway;
import com.lemnt.pay.infrastructure.event.dataobject.MessageInboxDO;
import com.lemnt.pay.infrastructure.event.mapper.MessageInboxMapper;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** 不能用短TTL缓存替代消费凭证，失败事务不得留下成功标记。 */
@Component
@RequiredArgsConstructor
public class MessageInboxGatewayImpl implements MessageInboxGateway {
    private final MessageInboxMapper mapper;
    @Override @Transactional(propagation=Propagation.MANDATORY,rollbackFor=Exception.class)
    public boolean has(String consumer,String type,String key){return mapper.lockBusiness(consumer,type,key)!=null;}


    @Override
    @Transactional(propagation = Propagation.MANDATORY, rollbackFor = Exception.class)
    public boolean begin(String consumer, String eventNo, String type, String key, String payload) {
        if (consumer == null
                || consumer.isBlank()
                || eventNo == null
                || eventNo.isBlank()
                || type == null
                || type.isBlank()
                || key == null
                || key.isBlank()
                || payload == null
                || payload.isBlank()) {
            throw new IllegalArgumentException("消费事件不完整");
        }
        String hash;
        try {
            hash =
                    HexFormat.of()
                            .formatHex(
                                    MessageDigest.getInstance("SHA-256")
                                            .digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256不可用", impossible);
        }
        try {
            mapper.insert(
                    new MessageInboxDO()
                            .setConsumerKey(consumer)
                            .setEventNo(eventNo)
                            .setEventType(type)
                            .setBusinessKey(key)
                            .setPayloadHash(hash));
            return true;
        } catch (DuplicateKeyException duplicate) {
            var existing = mapper.lockEvent(consumer, eventNo);
            if (existing == null) existing = mapper.lockBusiness(consumer, type, key);
            if (existing == null
                    || !type.equals(existing.getEventType())
                    || !key.equals(existing.getBusinessKey())
                    || !hash.equals(existing.getPayloadHash())) {
                throw new IllegalStateException("重复事件的冻结内容不一致", duplicate);
            }
            return false;
        }
    }
}
