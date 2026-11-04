package com.lemnt.order.adapter.web.settlement;

import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.promotion.adapter.web.coupon.CouponController;
import com.lemnt.promotion.adapter.web.coupon.CouponTemplateController;
import com.lemnt.promotion.application.coupon.cmd.CouponTemplateCreateCmd;
import com.lemnt.promotion.application.coupon.executor.CouponIssueCmdExe;
import com.lemnt.promotion.application.coupon.executor.CouponListQueryExe;
import com.lemnt.promotion.application.coupon.executor.CouponManagementPageQueryExe;
import com.lemnt.promotion.application.coupon.executor.CouponRevokeCmdExe;
import com.lemnt.promotion.application.coupon.executor.CouponTemplateCreateCmdExe;
import com.lemnt.promotion.application.coupon.executor.CouponTemplateDeleteCmdExe;
import com.lemnt.promotion.application.coupon.executor.CouponTemplateEnableCmdExe;
import com.lemnt.promotion.application.coupon.executor.CouponTemplateListQueryExe;
import com.lemnt.promotion.application.coupon.executor.CouponTemplateUpdateCmdExe;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CheckoutHttpContractTest {
    JacksonJsonHttpMessageConverter converter() {
        var builder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(builder);
        return new JacksonJsonHttpMessageConverter(builder.build());
    }

    @Test
    void couponValidityUsesExistingApplicationDateFormat() throws Exception {
        var create = mock(CouponTemplateCreateCmdExe.class);
        var controller =
            new Object[]{
                new CouponTemplateController(
                    mock(CouponTemplateListQueryExe.class),
                    create,
                    mock(CouponTemplateEnableCmdExe.class),
                    mock(CouponTemplateUpdateCmdExe.class),
                    mock(CouponTemplateDeleteCmdExe.class)),
                new CouponController(
                    mock(CouponIssueCmdExe.class),
                    mock(CouponRevokeCmdExe.class),
                    mock(CouponListQueryExe.class),
                    mock(CouponManagementPageQueryExe.class))
            };
        var mvc =
            MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(converter())
                .build();
        mvc.perform(
                post("/mall/admin/coupon-templates")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                            {"code":"C1","name":"coupon","kind":"FIXED","threshold":"10.00","fixedAmount":"3.00","totalCount":100,"validFrom":"2026-10-25 00:00:00","validUntil":"2026-11-25 00:00:00"}
                            """))
            .andExpect(status().isOk());
        var command = ArgumentCaptor.forClass(CouponTemplateCreateCmd.class);
        verify(create).execute(command.capture());
        assertEquals(25, command.getValue().validFrom().getDayOfMonth());
    }

    @Test
    void allAdminCheckoutActionsDeclarePermissions() {
        for (var type : List.of(CouponTemplateController.class, CouponController.class)) {
            for (var method : type.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                assertNotNull(method.getAnnotation(RequiresPermissions.class), method.toString());
            }
        }
    }
}
