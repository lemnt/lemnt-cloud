package com.lemnt.trade.adapter.web.delivery;

import com.lemnt.trade.application.delivery.cmd.DeliveryExpressTemplateCreateCmd;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressTemplateCreateCmdExe;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressTemplateUpdateCmdExe;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressTemplateDeleteCmdExe;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressTemplateDetailQueryExe;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressTemplatePageQueryExe;
import com.lemnt.trade.application.delivery.executor.DeliveryExpressTemplateSimpleListQueryExe;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 运费模板 HTTP 校验与独立输入转换，价格为整数分。 */
class DeliveryHttpTest {
    @Test
    void nestedRulesAreValidatedAndPassedWithoutChangingFenOrDecimals() throws Exception {
        var create = mock(DeliveryExpressTemplateCreateCmdExe.class);
        when(create.execute(any())).thenReturn(7L);
        var controller = new DeliveryExpressTemplateController(create,
                mock(DeliveryExpressTemplateUpdateCmdExe.class), mock(DeliveryExpressTemplateDeleteCmdExe.class),
                mock(DeliveryExpressTemplateDetailQueryExe.class), mock(DeliveryExpressTemplatePageQueryExe.class),
                mock(DeliveryExpressTemplateSimpleListQueryExe.class));
        var mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new com.lemnt.common.security.handler.GlobalExceptionHandler()).build();
        String valid = """
                {"name":"按重量配送","chargeMode":2,"sort":0,"charges":[
                {"areaIds":[110101],"startCount":1.5,"startPrice":600,"extraCount":0.5,"extraPrice":100}],
                "frees":[{"areaIds":[110101],"freePrice":20000,"freeCount":3}]}
                """;
        mvc.perform(post("/trade/admin/delivery/templates").contentType(MediaType.APPLICATION_JSON).content(valid))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").value(7));
        var input = ArgumentCaptor.forClass(DeliveryExpressTemplateCreateCmd.class);
        verify(create).execute(input.capture());
        assertEquals(new BigDecimal("1.5"), input.getValue().charges().get(0).startCount());
        assertEquals(600, input.getValue().charges().get(0).startPrice());
        mvc.perform(post("/trade/admin/delivery/templates").contentType(MediaType.APPLICATION_JSON)
                        .content(valid.replace("\"extraCount\":0.5", "\"extraCount\":0")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mvc.perform(post("/trade/admin/delivery/templates").contentType(MediaType.APPLICATION_JSON)
                        .content(valid.replace("[110101]", "[null]")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        verifyNoMoreInteractions(create);
        reset(create);
        when(create.execute(any())).thenThrow(new com.lemnt.common.core.exception.ServiceException(
                "配送区域不合法或重复", 400));
        mvc.perform(post("/trade/admin/delivery/templates").contentType(MediaType.APPLICATION_JSON).content(valid))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }
}
