package com.lemnt.mall.start.product;

import com.lemnt.mall.application.promotion.cmd.CombinationActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.CombinationProductCmd;
import com.lemnt.mall.application.promotion.cmd.CombinationRecordCreateCmd;
import com.lemnt.mall.application.promotion.service.CombinationActivityManagementService;
import com.lemnt.mall.application.promotion.service.CombinationRecordService;
import com.lemnt.mall.domain.member.gateway.MemberLookupGateway;
import com.lemnt.mall.domain.member.model.MemberSnapshot;
import com.lemnt.mall.domain.product.model.ProductSpu;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 生产 Mapper/XML 与事务代理验证拼团活动、开团参团和过期处理。 */
class CombinationPersistenceTest {
    CatalogPersistenceTest f;
    CombinationActivityManagementService activities;
    CombinationRecordService records;
    ProductSpu spu;
    Long activityId;

    @BeforeEach
    void setup() throws Exception {
        f = new CatalogPersistenceTest();
        f.setup();
        ScriptUtils.executeSqlScript(f.schemaConnection, new ClassPathResource("sql/V19__promotion_combination.sql"));
        f.components.register(MemberLookupGateway.class, memberId -> new MemberSnapshot(memberId, 1, 100, 0L,
                "会员" + memberId, "https://example.test/avatar.png"));
        activities = f.components.get(CombinationActivityManagementService.class);
        records = f.components.get(CombinationRecordService.class);
        spu = f.create("COMBINATION", f.draft("RED", 10), f.draft("BLUE", 10));
        var now = LocalDateTime.now();
        activityId = activities.create(new CombinationActivityCreateCmd("两人团", spu.id(), 10, 5,
                now.minusDays(1), now.plusDays(1), 2, true, 1,
                List.of(new CombinationProductCmd(spu.skus().get(0).id(), new BigDecimal("5.00")))));
    }

    @AfterEach
    void cleanup() throws Exception {
        f.closeSchemaConnection();
    }

    @Test
    void firstUserOpensGroupAndSecondUserCompletesIt() {
        var skuId = spu.skus().get(0).id();
        var head = records.create(new CombinationRecordCreateCmd(1L, spu.id(), activityId, null, skuId, 1));
        assertEquals(0, head.status());
        assertEquals(0L, head.headId());
        var member = records.create(new CombinationRecordCreateCmd(2L, spu.id(), activityId, head.id(), skuId, 1));
        assertEquals(1, member.status());
        var detail = records.detail(head.id(), 1L);
        assertEquals(1, detail.headRecord().status());
        assertEquals(1, detail.memberRecords().size());
        assertEquals(2, detail.headRecord().userCount());
    }

    @Test
    void sameUserCannotJoinTwoInProgressGroupsAndLimitsApply() {
        var skuId = spu.skus().get(0).id();
        records.create(new CombinationRecordCreateCmd(1L, spu.id(), activityId, null, skuId, 1));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> records.create(new CombinationRecordCreateCmd(1L, spu.id(), activityId, null, skuId, 1)));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> records.create(new CombinationRecordCreateCmd(2L, spu.id(), activityId, null, skuId, 6)));
    }

    @Test
    void expiredGroupWithoutVirtualMemberFails() {
        activities.close(activityId);
        var now = LocalDateTime.now();
        var id = activities.create(new CombinationActivityCreateCmd("失败团", spu.id(), 10, 5,
                now.minusDays(1), now.plusDays(1), 2, false, 1,
                List.of(new CombinationProductCmd(spu.skus().get(0).id(), new BigDecimal("5.00")))));
        var head = records.create(new CombinationRecordCreateCmd(1L, spu.id(), id, null,
                spu.skus().get(0).id(), 1));
        var gateway = f.components.get(com.lemnt.mall.domain.promotion.gateway.CombinationRecordGateway.class);
        gateway.save(new com.lemnt.mall.domain.promotion.model.CombinationRecord(head.id(), head.activityId(),
                head.combinationPrice(), head.spuId(), head.spuName(), head.picUrl(), head.skuId(), 1, 1L,
                head.nickname(), head.avatar(), 0L, 0, null, 2, 1, false,
                now.minusMinutes(1), head.startTime(), null));
        assertEquals(1, records.expire());
        assertEquals(2, records.detail(head.id(), 1L).headRecord().status());
    }

    @Test
    void expiredVirtualGroupCompletesWithPlaceholderMembers() {
        var head = records.create(new CombinationRecordCreateCmd(1L, spu.id(), activityId, null,
                spu.skus().get(0).id(), 1));
        var gateway = f.components.get(com.lemnt.mall.domain.promotion.gateway.CombinationRecordGateway.class);
        gateway.save(new com.lemnt.mall.domain.promotion.model.CombinationRecord(head.id(), head.activityId(),
                head.combinationPrice(), head.spuId(), head.spuName(), head.picUrl(), head.skuId(), 1, 1L,
                head.nickname(), head.avatar(), 0L, 0, null, 2, 1, false,
                LocalDateTime.now().minusMinutes(1), head.startTime(), null));
        assertEquals(1, records.expire());
        var detail = records.detail(head.id(), 1L);
        assertEquals(1, detail.headRecord().status());
        assertEquals(1, detail.memberRecords().size());
        assertTrue(detail.memberRecords().get(0).virtualGroup());
    }
}
