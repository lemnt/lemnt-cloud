package com.lemnt.promotion.start.promotion;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.promotion.application.promotion.cmd.AppKefuMessageSendCmd;
import com.lemnt.promotion.application.promotion.cmd.KefuConversationPinCmd;
import com.lemnt.promotion.application.promotion.cmd.KefuMessageSendCmd;
import com.lemnt.promotion.application.promotion.qry.AppKefuMessageQry;
import com.lemnt.promotion.application.promotion.qry.KefuMessageQry;
import com.lemnt.promotion.application.promotion.service.KefuChatService;
import com.lemnt.promotion.domain.member.gateway.MemberLookupGateway;
import com.lemnt.promotion.domain.member.model.MemberSnapshot;
import com.lemnt.promotion.start.TableTestComponents;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.sql.Connection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** 生产 SQL/Gateway/事务代理验证客服会话、未读数与已读。 */
class KefuPersistenceTest {
    JdbcDataSource source;
    Connection schema;
    KefuChatService service;

    @BeforeEach
    void setup() throws Exception {
        source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:kefu_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        schema = source.getConnection();
        ScriptUtils.executeSqlScript(schema, new ClassPathResource("promotion-sql/lemnt_promotion.sql"));
        var config = new MybatisConfiguration();
        config.setMapUnderscoreToCamelCase(true);
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        config.addInterceptor(interceptor);
        var bean = new MybatisSqlSessionFactoryBean();
        bean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath*:mapper/*.xml"));
        bean.setDataSource(source);
        bean.setGlobalConfig(new GlobalConfig().setMetaObjectHandler(new MyMetaObjectHandler()));
        bean.setConfiguration(config);
        var factory = bean.getObject();
        config.addMappers("com.lemnt.promotion.infrastructure");
        var components = new TableTestComponents(new SqlSessionTemplate(factory), source);
        components.register(
                MemberLookupGateway.class,
                new MemberLookupGateway() {
                    @Override
                    public MemberSnapshot findById(Long memberId) {
                        return new MemberSnapshot(
                                memberId,
                                0,
                                100,
                                0L,
                                "会员" + memberId,
                                "https://example.test/avatar.png");
                    }
                });
        service = components.get(KefuChatService.class);
    }

    @AfterEach
    void close() throws Exception {
        if (schema != null) {
            schema.close();
        }
    }

    Long send(Long memberId, String text) {
        return service.appSend(
                new AppKefuMessageSendCmd(memberId, text, KefuChatService.CONTENT_TEXT));
    }

    @Test
    void appMessageCreatesConversationAndIncrementsAdminUnread() {
        assertNull(service.appConversation(1L));
        send(1L, "你好");
        var conversation = service.appConversation(1L);
        assertNotNull(conversation.id());
        assertEquals("你好", conversation.lastMessageContent());
        assertEquals(0L, conversation.unreadCount().longValue());
        var admin =
                service.adminList().stream()
                        .filter(row -> row.userId().equals(1L))
                        .findFirst()
                        .orElseThrow();
        assertEquals(1, admin.adminUnreadMessageCount());
        assertEquals("会员1", admin.nickname());
    }

    @Test
    void adminReplyReadsAndPinnedOrderingAreConsistent() {
        send(1L, "问题");
        var id = service.adminList().get(0).id();
        service.adminSend(new KefuMessageSendCmd(id, 99L, "回复", KefuChatService.CONTENT_TEXT));
        var messages = service.appMessages(new AppKefuMessageQry(1L, null, 10));
        assertEquals(2, messages.size());
        assertEquals(1, messages.get(0).senderType().intValue());
        assertEquals(2, messages.get(1).senderType().intValue());
        assertEquals(1L, service.appConversation(1L).unreadCount().longValue());
        service.appRead(1L);
        assertEquals(0L, service.appConversation(1L).unreadCount().longValue());

        send(2L, "另一个会员");
        var second =
                service.adminList().stream()
                        .filter(row -> row.userId().equals(2L))
                        .findFirst()
                        .orElseThrow();
        service.adminPin(new KefuConversationPinCmd(second.id(), true));
        assertEquals(second.id(), service.adminList().get(0).id());
        service.adminRead(second.id());
        assertEquals(0, service.adminDetail(second.id()).adminUnreadMessageCount().intValue());
        assertEquals(1, service.adminMessages(new KefuMessageQry(second.id(), null, 10)).size());
    }

    @Test
    void adminDeleteHidesConversationUntilMemberSendsAgain() {
        send(1L, "第一次");
        var id = service.adminList().get(0).id();
        service.adminDelete(id);
        assertTrue(service.adminList().isEmpty());
        send(1L, "第二次");
        assertEquals(1, service.adminList().size());
        assertEquals("第二次", service.adminList().get(0).lastMessageContent());
    }

    @Test
    void invalidContentAndImageUrlAreRejected() {
        assertThrows(
                ServiceException.class,
                () ->
                        service.appSend(
                                new AppKefuMessageSendCmd(1L, " ", KefuChatService.CONTENT_TEXT)));
        assertThrows(
                ServiceException.class,
                () ->
                        service.appSend(
                                new AppKefuMessageSendCmd(
                                        1L, "ftp://x", KefuChatService.CONTENT_IMAGE)));
        assertThrows(
                ServiceException.class,
                () -> service.appSend(new AppKefuMessageSendCmd(1L, "x", 9)));
    }
}
