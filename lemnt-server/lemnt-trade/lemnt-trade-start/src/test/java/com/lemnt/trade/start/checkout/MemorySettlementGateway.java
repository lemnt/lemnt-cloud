package com.lemnt.trade.start.checkout;

import com.lemnt.mall.domain.settlement.gateway.SettlementGateway;
import com.lemnt.mall.domain.settlement.model.Settlement;
import com.lemnt.mall.domain.settlement.model.SettlementRecord;
import com.lemnt.mall.domain.settlement.model.SettlementSubmission;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** 结算缓存的测试替身；不代表生产 Redis Lua 和网络验收。 */
final class MemorySettlementGateway implements SettlementGateway {
    final Map<String, Settlement> previews;
    final Map<String, SettlementSubmission> submissions = new ConcurrentHashMap<>();
    final Map<String, String> requests = new ConcurrentHashMap<>();
    final Map<String, String> bindings = new ConcurrentHashMap<>();
    final Set<String> abandoned = ConcurrentHashMap.newKeySet();

    MemorySettlementGateway(Map<String, Settlement> previews) {
        this.previews = previews;
    }

    public void cache(Settlement value) { previews.put(value.id(), value); }
    public Settlement cached(String id) { return previews.get(id); }
    public SettlementSubmission submission(String no) { return submissions.get(no); }
    public SettlementRecord findRequest(Long member, String request) {
        var value = submissions.get(requests.getOrDefault(member + ":" + request, ""));
        return value == null ? null : value.record();
    }
    public synchronized void saveSubmission(SettlementSubmission value) {
        var record = value.record();
        String request = record.memberId() + ":" + record.requestNo();
        var previous = requests.putIfAbsent(request, record.orderNo());
        if (previous != null && !previous.equals(record.orderNo())) { throw new IllegalStateException("重复请求"); }
        submissions.put(record.orderNo(), value);
        rollback(() -> { submissions.remove(record.orderNo(), value); requests.remove(request, record.orderNo()); });
    }
    public List<String> ordersForCart(Long id) {
        return submissions.values().stream().filter(value -> value.record().cartReferences().stream()
                .anyMatch(ref -> ref.cartId().equals(id))).map(value -> value.record().orderNo()).toList();
    }
    public String orderNo(Long member, String id) { return bindings.get(member + ":" + id); }
    public synchronized void bind(Long member, String id, String no) {
        String key = member + ":" + id;
        if (abandoned.contains(key)) { throw new IllegalStateException("结算已放弃"); }
        var previous = bindings.putIfAbsent(key, no);
        if (previous != null && !previous.equals(no)) { throw new IllegalStateException("结算已绑定"); }
        rollback(() -> bindings.remove(key, no));
    }
    public boolean abandoned(Long member, String id) { return abandoned.contains(member + ":" + id); }
    public synchronized void abandon(Long member, String id) {
        String key = member + ":" + id;
        if (bindings.containsKey(key)) { throw new IllegalStateException("结算已绑定"); }
        abandoned.add(key);
        rollback(() -> abandoned.remove(key));
    }
    private void rollback(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                public int getOrder() { return Integer.MIN_VALUE; }
                public void afterCompletion(int status) { if (status == STATUS_ROLLED_BACK) { action.run(); } }
            });
        }
    }
}
