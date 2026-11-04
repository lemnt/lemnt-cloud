package com.lemnt.order.start.support;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.core.utils.JsonUtils;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** 测试Broker传输：发送确认与消费分离；消费失败保留原信封，重投不重算冻结内容。 */
public final class CommerceEventPump {
    private final CommerceTestFixture fixture;
    private final Map<String, CommerceEventEnvelope> pending = new LinkedHashMap<>();
    private final Map<String, CommerceEventEnvelope> delivered = new LinkedHashMap<>();
    public final Set<String> memberPaid = ConcurrentHashMap.newKeySet();
    public final Set<String> memberRefunded = ConcurrentHashMap.newKeySet();
    public Consumer<CommerceEventEnvelope> before = e -> {};
    public Consumer<CommerceEventEnvelope> afterProduct = e -> {};
    private RuntimeException lastFailure;

    public CommerceEventPump(CommerceTestFixture fixture) {
        this.fixture = fixture;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void install() throws Exception {
        for (String owner : List.of("product", "order", "pay")) {
            Class<?> type =
                    Class.forName(
                            "com.lemnt." + owner + ".domain.event.gateway.EventPublishGateway");
            Object publisher =
                    Proxy.newProxyInstance(
                            type.getClassLoader(),
                            new Class<?>[] {type},
                            (proxy, method, args) -> {
                                if (method.getName().equals("publish")) {
                                    Object message = args[0];
                                    String json =
                                            (String)
                                                    message.getClass()
                                                            .getMethod("payload")
                                                            .invoke(message);
                                    var event = JsonUtils.toBean(json, CommerceEventEnvelope.class);
                                    synchronized (this) {
                                        var old = pending.get(event.eventNo());
                                        if (old == null) old = delivered.get(event.eventNo());
                                        if (old != null && !old.payload().equals(event.payload()))
                                            throw new IllegalStateException("原事件载荷改变");
                                        pending.putIfAbsent(event.eventNo(), event);
                                    }
                                    return null;
                                }
                                return "测试Broker";
                            });
            fixture.register((Class) type, publisher);
        }
        fixture.after(
                (type, method) -> {
                    if (type.getName()
                                    .equals(
                                            "com.lemnt.pay.application.payment.executor.PaymentNotifyCmdExe")
                            && method.getName().equals("execute")) drainOrThrow();
                    if (type.getName()
                                    .equals(
                                            "com.lemnt.order.application.paymentrefund.executor.PaymentRefundCreateCmdExe")
                            && method.getName().startsWith("execute")) drain();
                });
    }

    public synchronized List<CommerceEventEnvelope> pendingEvents() {
        return List.copyOf(pending.values());
    }

    public synchronized void replay(String eventNo) {
        var event = delivered.get(eventNo);
        if (event == null) throw new IllegalArgumentException("原已投递事件不存在");
        pending.put(eventNo, event);
    }

    public void drainOrThrow() {
        drain();
        if (lastFailure != null) throw lastFailure;
    }

    /** 每轮失败只尝试一次，下次显式驱动才重投；源Outbox使用真实租约和发送确认。 */
    public int drain() {
        return fixture.call(
                () -> {
                    lastFailure = null;
                    var failed = new HashSet<String>();
                    int closed = 0;
                    for (int round = 0; round < 5; round++) {
                        int sentBefore = delivered.size();
                        for (String owner : List.of("product", "pay", "order")) {
                            fixture.jdbc(owner)
                                    .update(
                                            "UPDATE "
                                                    + owner
                                                    + "_message_outbox SET next_retry_at=TIMESTAMPADD(SECOND,-1,CURRENT_TIMESTAMP) WHERE status='PENDING'");
                            fixture.invoke(
                                    "com.lemnt."
                                            + owner
                                            + ".application.event.service.OutboxRelayService",
                                    "relay");
                        }
                        List<CommerceEventEnvelope> batch;
                        synchronized (this) {
                            batch = new ArrayList<>(pending.values());
                        }
                        for (var event : batch) {
                            if (failed.contains(event.eventNo())) continue;
                            try {
                                deliver(event);
                                synchronized (this) {
                                    pending.remove(event.eventNo());
                                    delivered.put(event.eventNo(), event);
                                }
                                if ("ORDER_CLOSED".equals(event.eventType())) closed++;
                            } catch (RuntimeException e) {
                                failed.add(event.eventNo());
                                lastFailure = e;
                            }
                        }
                        if (delivered.size() == sentBefore) break;
                    }
                    return closed;
                });
    }

    private void handler(String name, String method, CommerceEventEnvelope e) {
        fixture.invoke(name, method, e);
    }

    private void deliver(CommerceEventEnvelope e) {
        before.accept(e);
        switch (e.eventType()) {
            case "PAY_SUCCESS" ->
                    handler(
                            "com.lemnt.order.application.event.service.PaySuccessEventHandler",
                            "execute",
                            e);
            case "PAY_REFUND_SUCCESS" ->
                    handler(
                            "com.lemnt.order.application.event.service.PayRefundSuccessEventHandler",
                            "execute",
                            e);
            case "STOCK_PRE_RESERVED" ->
                    handler(
                            "com.lemnt.promotion.application.event.service.StockPreReservedEventHandler",
                            "execute",
                            e);
            case "ORDER_PAID" -> {
                handler(
                        "com.lemnt.product.application.event.service.OrderStockEventHandler",
                        "paid",
                        e);
                afterProduct.accept(e);
                handler(
                        "com.lemnt.promotion.application.event.service.OrderPaidEventHandler",
                        "execute",
                        e);
                memberPaid.add(e.aggregateNo());
                fixture.invoke(
                        "com.lemnt.order.application.order.service.OrderRemoteService",
                        "paid",
                        e.aggregateNo());
            }
            case "ORDER_CLOSED" -> {
                handler(
                        "com.lemnt.pay.application.event.service.OrderClosedEventHandler",
                        "execute",
                        e);
                handler(
                        "com.lemnt.product.application.event.service.OrderStockEventHandler",
                        "closed",
                        e);
                handler(
                        "com.lemnt.promotion.application.event.service.OrderClosedEventHandler",
                        "execute",
                        e);
            }
            case "AFTER_SALE_COMPLETED" -> {
                handler(
                        "com.lemnt.product.application.event.service.OrderStockEventHandler",
                        "refunded",
                        e);
                handler(
                        "com.lemnt.promotion.application.event.service.AfterSaleCompletedEventHandler",
                        "execute",
                        e);
                memberRefunded.add(e.businessKey());
            }
            case "ORDER_COMPLETED" ->
                    handler(
                            "com.lemnt.promotion.application.event.service.OrderCompletedEventHandler",
                            "execute",
                            e);
            case "ORDER_SHIPPED" -> {}
            default -> throw new IllegalArgumentException("未知业务Topic:" + e.eventType());
        }
    }
}
