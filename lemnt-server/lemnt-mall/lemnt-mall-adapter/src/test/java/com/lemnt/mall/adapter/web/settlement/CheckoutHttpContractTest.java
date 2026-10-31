package com.lemnt.mall.adapter.web.settlement;

import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.mall.adapter.web.coupon.CouponController;
import com.lemnt.mall.adapter.web.coupon.CouponTemplateController;
import com.lemnt.mall.application.coupon.cmd.CouponTemplateCreateCmd;
import com.lemnt.mall.application.coupon.executor.CouponIssueCmdExe;
import com.lemnt.mall.application.coupon.executor.CouponListQueryExe;
import com.lemnt.mall.application.coupon.executor.CouponTemplateCreateCmdExe;
import com.lemnt.mall.application.coupon.executor.CouponTemplateEnableCmdExe;
import com.lemnt.mall.application.coupon.executor.CouponTemplateListQueryExe;

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
        new Object[] {
                new CouponTemplateController(mock(CouponTemplateListQueryExe.class), create, mock(CouponTemplateEnableCmdExe.class), mock(com.lemnt.mall.application.coupon.executor.CouponTemplateUpdateCmdExe.class), mock(com.lemnt.mall.application.coupon.executor.CouponTemplateDeleteCmdExe.class)),
                new CouponController(mock(CouponIssueCmdExe.class), mock(com.lemnt.mall.application.coupon.executor.CouponRevokeCmdExe.class), mock(CouponListQueryExe.class), mock(com.lemnt.mall.application.coupon.executor.CouponManagementPageQueryExe.class))
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
        var command =
        ArgumentCaptor.forClass(
                CouponTemplateCreateCmd.class);
        verify(create).execute(command.capture());
        assertEquals(25, command.getValue().validFrom().getDayOfMonth());
    }

    @Test
    void allAdminCheckoutActionsDeclarePermissions() {
        for (var type :
                List.of(
                CouponTemplateController.class, CouponController.class)) {
            for (var method : type.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                assertNotNull(method.getAnnotation(RequiresPermissions.class), method.toString());
            }
        }
    }
}
