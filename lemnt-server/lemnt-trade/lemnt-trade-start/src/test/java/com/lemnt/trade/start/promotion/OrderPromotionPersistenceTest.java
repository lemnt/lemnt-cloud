package com.lemnt.trade.start.promotion;

import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.trade.domain.order.model.TradeOrderItem;
import com.lemnt.trade.domain.promotion.gateway.OrderPromotionGateway;
import com.lemnt.trade.domain.promotion.model.*;
import com.lemnt.trade.infrastructure.order.gateway.TradeOrderItemGatewayImpl;
import com.lemnt.trade.infrastructure.order.mapper.TradeOrderItemMapper;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** 生产DDL和Mapper验证三表、金额快照、事务回滚和已退数量CAS，非目标MySQL验收。 */
class OrderPromotionPersistenceTest {
    AnnotationConfigApplicationContext context;
    OrderPromotionGateway promotions;
    JdbcTemplate jdbc;
    final BigDecimal zero = new BigDecimal("0.00");

    @BeforeEach
    void setup() {
        context = new AnnotationConfigApplicationContext(TestConfig.class);
        promotions = context.getBean(OrderPromotionGateway.class);
        jdbc = new JdbcTemplate(context.getBean(JdbcDataSource.class));
        jdbc.update(
                "INSERT INTO trade_order(id,order_no,member_id,total_amount,status,use_point,give_point) VALUES(7,'T1',1,10,'PAID',5,20)");
        jdbc.update(
                "INSERT INTO trade_order_item(id,order_id,sku_id,product_code,product_name,unit_price,quantity,line_amount,pay_amount,delivery_price,refunded_quantity) VALUES(11,7,2,'SKU','商品',6,2,12,10,2,0)");
    }

    @AfterEach
    void close() {
        context.close();
    }

    TradeOrderItem item() {
        return new TradeOrderItem(
                2L,
                "SKU",
                "商品",
                new BigDecimal("6.00"),
                2,
                new BigDecimal("12.00"),
                new BigDecimal("10.00"),
                new BigDecimal("2.00"),
                null,
                null,
                null,
                null,
                11L,
                false);
    }

    OrderPromotion promotion(String lineKey) {
        var line =
                new OrderItemPromotion(
                        null,
                        null,
                        null,
                        lineKey,
                        new BigDecimal("2.00"),
                        new BigDecimal("1.00"),
                        5L,
                        20L);
        return new OrderPromotion(
                null,
                null,
                "POINT:1",
                "POINT_EXCHANGE",
                1L,
                "兑换活动",
                "活动价",
                "{}",
                new BigDecimal("2.00"),
                new BigDecimal("1.00"),
                5L,
                20L,
                Map.of(),
                List.of(line));
    }

    @Test
    void headerLineAndRefundFactsRoundTrip() {
        promotions.save(7L, List.of(promotion("0")), List.of(item()));
        var header = promotions.find(7L).get(0);
        var line = header.items().get(0);
        assertEquals(11L, line.orderItemId());
        assertEquals("0", line.quoteLineKey());
        assertEquals(new BigDecimal("2.00"), line.discountAmount());
        assertEquals(5L, line.usePoint());
        insertAfterSale(13, "RF1", 11);
        promotions.saveRefund(
                13L,
                List.of(
                        new OrderPromotionRefund(
                                null,
                                null,
                                line.id(),
                                new BigDecimal("1.00"),
                                new BigDecimal("0.50"),
                                2L,
                                10L)));
        var result = promotions.refunds(List.of(13L));
        assertEquals(1, result.size());
        assertEquals(new BigDecimal("1.00"), result.get(0).refundDiscountAmount());
        assertEquals(2L, result.get(0).returnUsePoint());
        assertThrows(
                DataIntegrityViolationException.class,
                () ->
                        promotions.saveRefund(
                                13L,
                                List.of(
                                        new OrderPromotionRefund(
                                                null, null, line.id(), zero, zero, 0L, 0L))));
    }

    @Test
    void badLineMappingRollsBackAlreadyInsertedPromotionHeader() {
        var tx = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        tx.executeWithoutResult(
                                status ->
                                        promotions.save(
                                                7L, List.of(promotion("3")), List.of(item()))));
        assertTrue(promotions.find(7L).isEmpty());
    }

    @Test
    void refundedQuantityIsBoundedAndCannotBeAppliedTwice() {
        var items = new TradeOrderItemGatewayImpl(context.getBean(TradeOrderItemMapper.class));
        assertTrue(items.recordRefunded(7L, 11L, 0, 1));
        assertFalse(items.recordRefunded(7L, 11L, 0, 1));
        assertFalse(items.recordRefunded(7L, 11L, 1, 2));
        assertTrue(items.recordRefunded(7L, 11L, 1, 1));
        assertEquals(
                2,
                jdbc.queryForObject(
                        "SELECT refunded_quantity FROM trade_order_item WHERE id=11",
                        Integer.class));
    }

    @Test
    void existingActiveOrderIndexAlsoGuardsDifferentItems() {
        jdbc.update(
                "INSERT INTO trade_order_item(id,order_id,sku_id,product_code,product_name,unit_price,quantity,line_amount,pay_amount) VALUES(12,7,3,'SKU2','商品2',1,1,1,0)");
        insertAfterSale(13, "RF1", 11);
        assertThrows(DataIntegrityViolationException.class, () -> insertAfterSale(14, "RF2", 12));
    }

    private void insertAfterSale(long id, String no, long item) {
        jdbc.update(
                "INSERT INTO trade_after_sale(id,refund_no,order_no,request_no,member_id,order_item_id,quantity,type,amount,reason,status,active_order_no,requested_at) VALUES(?,?,'T1',?,1,?,1,'REFUND_ONLY',0,'原因','REQUESTED','T1',CURRENT_TIMESTAMP)",
                id,
                no,
                no,
                item);
    }

    @Configuration
    @EnableTransactionManagement(proxyTargetClass = true)
    @ComponentScan(basePackages = {"com.lemnt.trade.infrastructure.promotion.gateway"})
    @MapperScan(
            basePackages = {
                "com.lemnt.trade.infrastructure.promotion.mapper",
                "com.lemnt.trade.infrastructure.order.mapper"
            })
    static class TestConfig {
        @Bean
        JdbcDataSource dataSource() {
            var source = new JdbcDataSource();
            source.setURL(
                    "jdbc:h2:mem:promotion_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
            return source;
        }

        @Bean(destroyMethod = "close")
        Connection schema(JdbcDataSource source) throws Exception {
            var connection = source.getConnection();
            ScriptUtils.executeSqlScript(
                    connection, new ClassPathResource("trade-sql/lemnt_trade.sql"));
            return connection;
        }

        @Bean
        SqlSessionFactory sqlSessionFactory(JdbcDataSource source, Connection schema)
                throws Exception {
            var config = new MybatisConfiguration();
            config.setMapUnderscoreToCamelCase(true);
            var interceptor = new MybatisPlusInterceptor();
            interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
            config.addInterceptor(interceptor);
            var factory = new MybatisSqlSessionFactoryBean();
            factory.setDataSource(source);
            factory.setConfiguration(config);
            factory.setGlobalConfig(
                    new GlobalConfig().setMetaObjectHandler(new MyMetaObjectHandler()));
            factory.setMapperLocations(
                    new PathMatchingResourcePatternResolver()
                            .getResources("classpath*:mapper/*.xml"));
            return factory.getObject();
        }

        @Bean
        PlatformTransactionManager transactionManager(JdbcDataSource source) {
            return new DataSourceTransactionManager(source);
        }
    }
}
