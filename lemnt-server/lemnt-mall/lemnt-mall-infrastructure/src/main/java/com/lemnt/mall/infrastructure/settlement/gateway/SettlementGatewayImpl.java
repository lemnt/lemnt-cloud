package com.lemnt.mall.infrastructure.settlement.gateway;

import com.lemnt.common.redis.serializer.RedisSerializer;
import com.lemnt.mall.domain.settlement.gateway.SettlementGateway;
import com.lemnt.mall.domain.settlement.model.Settlement;
import com.lemnt.mall.domain.settlement.model.SettlementRecord;
import com.lemnt.mall.domain.settlement.model.SettlementSubmission;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** 结算上下文统一存 Redis，订单事实由交易服务维护。 */
@Component
@RequiredArgsConstructor
public class SettlementGatewayImpl implements SettlementGateway {
    private static final Duration RECOVERY_TTL = Duration.ofDays(30);
    private static final String PREFIX = "mall:{settlement}:v2:";
    private static final DefaultRedisScript<Long> SAVE = script("settlement-save");
    private static final DefaultRedisScript<Long> ROLLBACK = script("settlement-rollback");
    private static final DefaultRedisScript<Long> BIND = script("settlement-bind");
    private static final DefaultRedisScript<Long> ABANDON = script("settlement-abandon");
    private static final DefaultRedisScript<Long> REMOVE = script("settlement-remove-pointer");
    private final StringRedisTemplate redis;
    private final RedisSerializer serializer;

    /** 所有提交索引使用相同 hash tag，支持 Redis Cluster 的多键原子脚本。 */
    private static DefaultRedisScript<Long> script(String name) {
        var script = new DefaultRedisScript<Long>();
        script.setLocation(new ClassPathResource("redis/mall/" + name + ".lua"));
        script.setResultType(Long.class);
        return script;
    }

    /** 缓存结算预览，保留现有十五分钟有效期和 NX 约束。 */
    @Override
    public void cache(Settlement value) {
        var ttl = Duration.between(Instant.now(), value.expiresAt());
        if (ttl.isNegative() || ttl.isZero() || !Boolean.TRUE.equals(redis.opsForValue()
                .setIfAbsent("mall:settlement:v4:" + value.id(), serializer.serialize(value), ttl))
        ) {
            throw new IllegalStateException("结算单缓存写入失败");
        }
    }

    /** 只读取真实存在的预览，不生成替代报价。 */
    @Override
    public Settlement cached(String id) {
        var json = redis.opsForValue().get("mall:settlement:v4:" + id);
        return json == null ? null : serializer.deserialize(json, Settlement.class);
    }

    /** 提交快照和索引同时写入，数据库事务失败时撤销本次写入。 */
    @Override
    public void saveSubmission(SettlementSubmission value) {
        var record = value.record();
        var keys = new ArrayList<String>();
        keys.add(PREFIX + "submission:" + record.orderNo());
        keys.add(requestKey(record.memberId(), record.requestNo()));
        record.cartReferences().forEach(ref -> keys.add(PREFIX + "cart:" + ref.cartId()));
        String json = serializer.serialize(value);
        Long result = redis.execute(SAVE, keys, json, record.orderNo(), Long.toString(RECOVERY_TTL.toSeconds()));
        require(result);
        if (Long.valueOf(1).equals(result)
        ) {
            onRollback(() -> redis.execute(ROLLBACK, keys, json, record.orderNo()));
        }
    }

    /** 读取完整提交上下文。 */
    @Override
    public SettlementSubmission submission(String orderNo) {
        var json = redis.opsForValue().get(PREFIX + "submission:" + orderNo);
        return json == null ? null : serializer.deserialize(com.lemnt.mall.infrastructure.settlement.converter.SettlementSubmissionConverter.normalize(json), SettlementSubmission.class);
    }

    /** 请求索引只指向同一份提交内容。 */
    @Override
    public SettlementRecord findRequest(Long memberId, String requestNo) {
        var orderNo = redis.opsForValue().get(requestKey(memberId, requestNo));
        var value = orderNo == null ? null : submission(orderNo);
        if (orderNo != null && value == null
        ) {
            throw new IllegalStateException("提交缓存不完整，请按原订单号查询，禁止重复下单");
        }
        return value == null ? null : value.record();
    }

    /** 查询购物车引用，过滤已结束恢复窗口的索引。 */
    @Override
    public List<String> ordersForCart(Long cartId) {
        var orders = redis.opsForSet().members(PREFIX + "cart:" + cartId);
        return orders == null ? List.of() : orders.stream().filter(no -> submission(no) != null).toList();
    }

    /** 读取结算单唯一绑定。 */
    @Override
    public String orderNo(Long memberId, String id) {
        return redis.opsForValue().get(bindingKey(memberId, id));
    }

    /** 绑定与放弃标记互斥；重复绑定相同订单保持幂等。 */
    @Override
    public void bind(Long memberId, String id, String orderNo) {
        String key = bindingKey(memberId, id);
        Long result = redis.execute(BIND, List.of(key, abandonKey(memberId, id)), orderNo,
                Long.toString(RECOVERY_TTL.toSeconds()));
        require(result);
        if (Long.valueOf(1).equals(result)
        ) {
            onRollback(() -> redis.execute(REMOVE, List.of(key), orderNo));
        }
    }

    /** 读取放弃标记。 */
    @Override
    public boolean abandoned(Long memberId, String id) {
        return Boolean.TRUE.equals(redis.hasKey(abandonKey(memberId, id)));
    }

    /** 放弃标记与已绑定订单互斥，保留迟到提交保护。 */
    @Override
    public void abandon(Long memberId, String id) {
        String key = abandonKey(memberId, id);
        Long result = redis.execute(ABANDON, List.of(key, bindingKey(memberId, id)),
                Long.toString(RECOVERY_TTL.toSeconds()));
        require(result);
        if (Long.valueOf(1).equals(result)
        ) {
            onRollback(() -> redis.execute(REMOVE, List.of(key), "1"));
        }
    }

    private String requestKey(Long memberId, String requestNo) {
        return PREFIX + "request:" + memberId + ":" + requestNo;
    }

    private String bindingKey(Long memberId, String id) {
        return PREFIX + "binding:" + memberId + ":" + id;
    }

    private String abandonKey(Long memberId, String id) {
        return PREFIX + "abandon:" + memberId + ":" + id;
    }

    /** Redis 失败不降级为无索引下单。 */
    private void require(Long result) {
        if (!Long.valueOf(1).equals(result) && !Long.valueOf(2).equals(result)
        ) {
            throw new IllegalStateException("结算缓存写入失败或请求已被占用");
        }
    }

    /** 事务回滚后撤销缓存，不在数据库尚未确定时解锁会员操作。 */
    private void onRollback(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()
        ) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization(
            ) {
                @Override
                public int getOrder(
                ) {
                    return Integer.MIN_VALUE;
                }

                @Override
                public void afterCompletion(int status
                ) {
                    if (status == STATUS_ROLLED_BACK
                    ) {
                        action.run();
                    }
                }
            });
        }
    }
}
