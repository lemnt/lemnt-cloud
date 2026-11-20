package com.lemnt.order.application.brokerage;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.order.application.brokerage.cmd.BrokerageWithdrawAuditCmd;
import com.lemnt.order.application.brokerage.cmd.BrokerageWithdrawCreateCmd;
import com.lemnt.order.application.brokerage.qry.BrokerageRecordPageQry;
import com.lemnt.order.application.brokerage.qry.BrokerageWithdrawPageQry;
import com.lemnt.order.application.brokerage.service.BrokerageService;
import com.lemnt.order.application.brokerage.service.BrokerageWithdrawService;
import com.lemnt.order.application.order.service.OrderPersistenceService;
import com.lemnt.order.application.support.BusinessConfigTestSupport;
import com.lemnt.order.application.support.SqlTestSupport;
import com.lemnt.order.application.support.TableTestComponents;
import com.lemnt.order.domain.brokerage.gateway.BrokerageMemberGateway;
import com.lemnt.order.domain.brokerage.gateway.BrokerageWalletGateway;
import com.lemnt.order.domain.brokerage.model.BrokerageMemberInfo;
import com.lemnt.order.domain.config.gateway.TradeConfigGateway;
import com.lemnt.order.domain.config.model.TradeConfig;
import com.lemnt.order.domain.order.model.TradeOrder;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 生产 SQL/Gateway/事务代理验证绑定、两级佣金、解冻、退款取消与提现。
 */
class BrokeragePersistenceTest {
    JdbcDataSource source;
    Connection schema;
    BrokerageService brokerage;
    BrokerageWithdrawService withdraws;

    @BeforeEach
    void setup() throws Exception {
        source = new JdbcDataSource();
        source.setURL(
            "jdbc:h2:mem:brokerage_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        schema = source.getConnection();
        ScriptUtils.executeSqlScript(schema, new ClassPathResource("order-sql/lemnt_order.sql"));

        var components = new TableTestComponents(SqlTestSupport.session("order", source), source);
        components.register(
            BrokerageMemberGateway.class,
            id -> new BrokerageMemberInfo(id, "会员" + id, "https://example.test/avatar.png"));
        var orders = mock(OrderPersistenceService.class);
        when(orders.findByOrderNo("TR1"))
            .thenReturn(
                new TradeOrder(
                    1L,
                    "TR1",
                    1L,
                    new BigDecimal("100.00"),
                    "PAID",
                    List.of(),
                    null,
                    0,
                    0,
                    null));
        components.register(OrderPersistenceService.class, orders);
        components.register(TradeConfigGateway.class, BusinessConfigTestSupport.gateway());
        var configs = components.get(TradeConfigGateway.class);
        configs.update(
            new TradeConfig(
                1L,
                List.of(),
                List.of(),
                false,
                0,
                false,
                true,
                1,
                3,
                List.of(),
                10,
                5,
                100,
                5,
                3,
                List.of(1, 2)));
        components.register(BrokerageWalletGateway.class, mock(BrokerageWalletGateway.class));
        brokerage = components.get(BrokerageService.class);
        withdraws = components.get(BrokerageWithdrawService.class);
    }

    @AfterEach
    void close() throws Exception {
        if (schema != null) {
            schema.close();
        }
    }

    @Test
    void bindValidatesSelfCycleAndEnabledPromoter() {
        brokerage.setEnabled(2L, true);
        assertThrows(ServiceException.class, () -> brokerage.bind(1L, 1L));
        assertThrows(ServiceException.class, () -> brokerage.bind(1L, 3L));
        assertTrue(brokerage.bind(1L, 2L));
        assertEquals(2L, brokerage.appUser(1L).bindUserId());
        assertThrows(ServiceException.class, () -> brokerage.bind(2L, 1L));
        assertTrue(brokerage.adminBind(1L, null));
        assertNull(brokerage.appUser(1L).bindUserId());
    }

    @Test
    void orderCommissionCreditsTwoLevelsAndRefundCancels() {
        brokerage.setEnabled(2L, true);
        brokerage.setEnabled(3L, true);
        brokerage.bind(1L, 2L);
        brokerage.bind(2L, 3L);
        assertEquals(2, brokerage.addOrderCommission("TR1"));
        assertEquals(new BigDecimal("10.00"), brokerage.appUser(2L).frozenPrice());
        assertEquals(new BigDecimal("5.00"), brokerage.appUser(3L).frozenPrice());
        assertEquals(
            2,
            brokerage
                .pageRecords(new BrokerageRecordPageQry(null, null, null, 1, 20))
                .getTotal());
        assertEquals(0, brokerage.addOrderCommission("TR1"));
        assertEquals(2, brokerage.cancelOrderCommission("TR1"));
        assertEquals(0, brokerage.appUser(2L).frozenPrice().signum());
        assertEquals(
            BrokerageService.STATUS_CANCEL,
            brokerage
                .pageRecords(new BrokerageRecordPageQry(2L, null, null, 1, 20))
                .getRecords()
                .get(0)
                .status());
    }

    @Test
    void partialRefundRecallsCommissionProportionallyAndIsIdempotent() {
        brokerage.setEnabled(2L, true);
        brokerage.setEnabled(3L, true);
        brokerage.bind(1L, 2L);
        brokerage.bind(2L, 3L);
        assertEquals(2, brokerage.addOrderCommission("TR1"));

        assertEquals(
            2,
            brokerage.recallOrderCommission(
                "TR1", new BigDecimal("40.00"), new BigDecimal("100.00")));
        assertEquals(new BigDecimal("6.00"), brokerage.appUser(2L).frozenPrice());
        assertEquals(new BigDecimal("3.00"), brokerage.appUser(3L).frozenPrice());
        assertEquals(
            0,
            brokerage.recallOrderCommission(
                "TR1", new BigDecimal("40.00"), new BigDecimal("100.00")));
        assertEquals(new BigDecimal("6.00"), brokerage.appUser(2L).frozenPrice());

        assertEquals(2, brokerage.cancelOrderCommission("TR1"));
        assertEquals(0, brokerage.appUser(2L).frozenPrice().signum());
        assertEquals(0, brokerage.appUser(3L).frozenPrice().signum());
    }

    @Test
    void unfreezeMovesFrozenToAvailable() throws Exception {
        brokerage.setEnabled(2L, true);
        brokerage.bind(1L, 2L);
        brokerage.addOrderCommission("TR1");
        assertEquals(new BigDecimal("10.00"), brokerage.appUser(2L).frozenPrice());
        try (var statement = schema.createStatement()) {
            statement.execute(
                "UPDATE order_brokerage_record SET unfreeze_time=DATEADD('DAY', -1, CURRENT_TIMESTAMP)");
        }
        assertEquals(1, brokerage.unfreezeDue(10));
        assertEquals(new BigDecimal("10.00"), brokerage.appUser(2L).brokeragePrice());
        assertEquals(0, brokerage.appUser(2L).frozenPrice().signum());
        assertEquals(0, brokerage.unfreezeDue(10));
    }

    @Test
    void withdrawDeductsAndAuditRejectReturns() {
        brokerage.setEnabled(2L, true);
        brokerage.bind(1L, 2L);
        brokerage.addOrderCommission("TR1");
        try (var statement = schema.createStatement()) {
            statement.execute(
                "UPDATE order_brokerage_record SET unfreeze_time=DATEADD('DAY', -1, CURRENT_TIMESTAMP)");
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
        brokerage.unfreezeDue(10);
        var withdrawId =
            withdraws.create(
                new BrokerageWithdrawCreateCmd(
                    2L, new BigDecimal("4.00"), 1, null, null, null, null, null));
        assertEquals(new BigDecimal("6.00"), brokerage.appUser(2L).brokeragePrice());
        assertTrue(withdraws.audit(new BrokerageWithdrawAuditCmd(withdrawId, true, "通过", 99L)));
        assertEquals(
            11,
            withdraws
                .page(new BrokerageWithdrawPageQry(2L, null, 1, 20))
                .getRecords()
                .get(0)
                .status()
                .intValue());
        var second =
            withdraws.create(
                new BrokerageWithdrawCreateCmd(
                    2L, new BigDecimal("2.00"), 1, null, null, null, null, null));
        assertTrue(withdraws.audit(new BrokerageWithdrawAuditCmd(second, false, "驳回", 99L)));
        assertEquals(new BigDecimal("6.00"), brokerage.appUser(2L).brokeragePrice());
        assertThrows(
            ServiceException.class,
            () ->
                withdraws.create(
                    new BrokerageWithdrawCreateCmd(
                        2L,
                        new BigDecimal("0.50"),
                        1,
                        null,
                        null,
                        null,
                        null,
                        null)));
    }
}
