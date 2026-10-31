package com.lemnt.trade.adapter.app.aftersale;

import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.trade.application.aftersale.dto.AfterSaleDTO;
import com.lemnt.trade.application.aftersale.executor.AfterSaleCancelCmdExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleCreateCmdExe;
import com.lemnt.trade.application.aftersale.executor.AfterSaleReturnCmdExe;
import com.lemnt.trade.application.aftersale.executor.MemberAfterSaleDetailQueryExe;
import com.lemnt.trade.application.aftersale.executor.MemberAfterSaleItemListQueryExe;
import com.lemnt.trade.application.aftersale.executor.MemberAfterSalePageQueryExe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 本人售后只取已经认证的上下文，不接收客户端会员编号。 */
class AppAfterSaleControllerTest {
    @AfterEach
    void clear() { SecurityContextHolder.remove(); }

    private AppAfterSaleController controller(AfterSaleCancelCmdExe cancel, AfterSaleCreateCmdExe create,
            AfterSaleReturnCmdExe returned, MemberAfterSalePageQueryExe page,
            MemberAfterSaleDetailQueryExe detail, MemberAfterSaleItemListQueryExe items) {
        return new AppAfterSaleController(cancel, create, returned, page, detail, items);
    }

    private AppAfterSaleController withCancel(AfterSaleCancelCmdExe cancel) {
        return controller(cancel, mock(AfterSaleCreateCmdExe.class), mock(AfterSaleReturnCmdExe.class),
                mock(MemberAfterSalePageQueryExe.class), mock(MemberAfterSaleDetailQueryExe.class),
                mock(MemberAfterSaleItemListQueryExe.class));
    }

    private AfterSaleDTO dto() {
        return new AfterSaleDTO("RF1", "TR1", "REQ1", 1L, "P1", 10L, 1, "REFUND_ONLY", BigDecimal.ONE, "r",
                "REQUESTED", null, null, null, null, null, null, null);
    }

    @Test
    void cancellationIgnoresForgedMemberInput() throws Exception {
        SecurityContextHolder.setUserId("1");
        var cancel = mock(AfterSaleCancelCmdExe.class);
        when(cancel.execute(1L, "RF1")).thenReturn(true);
        var mvc = MockMvcBuilders.standaloneSetup(withCancel(cancel)).build();
        mvc.perform(post("/trade/app/after-sales/RF1/cancel").header("X-User-Id", "999")
                .contentType(MediaType.APPLICATION_JSON).content("{\"memberId\":999}"))
                .andExpect(status().isOk());
        verify(cancel).execute(1L, "RF1");
        verifyNoMoreInteractions(cancel);
    }

    @Test
    void itemsAndDetailUseVerifiedMember() throws Exception {
        SecurityContextHolder.setUserId("1");
        var items = mock(MemberAfterSaleItemListQueryExe.class);
        when(items.execute(1L, "TR1")).thenReturn(List.of());
        var detail = mock(MemberAfterSaleDetailQueryExe.class);
        when(detail.execute(1L, "RF1")).thenReturn(dto());
        var mvc = MockMvcBuilders.standaloneSetup(controller(mock(AfterSaleCancelCmdExe.class),
                mock(AfterSaleCreateCmdExe.class), mock(AfterSaleReturnCmdExe.class),
                mock(MemberAfterSalePageQueryExe.class), detail, items)).build();
        mvc.perform(get("/trade/app/orders/TR1/after-sale-items")).andExpect(status().isOk());
        mvc.perform(get("/trade/app/after-sales/RF1")).andExpect(status().isOk());
        verify(items).execute(1L, "TR1");
        verify(detail).execute(1L, "RF1");
    }

    @Test
    void createUsesVerifiedMemberAndServerCalculatesAmount() throws Exception {
        SecurityContextHolder.setUserId("1");
        var create = mock(AfterSaleCreateCmdExe.class);
        when(create.executeForMember(any(), eq(1L))).thenReturn(dto());
        var mvc = MockMvcBuilders.standaloneSetup(controller(mock(AfterSaleCancelCmdExe.class), create,
                mock(AfterSaleReturnCmdExe.class), mock(MemberAfterSalePageQueryExe.class),
                mock(MemberAfterSaleDetailQueryExe.class), mock(MemberAfterSaleItemListQueryExe.class))).build();
        mvc.perform(post("/trade/app/orders/TR1/after-sales").header("X-User-Id", "999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"requestNo\":\"REQ1\",\"orderItemId\":10,\"quantity\":1,"
                        + "\"type\":\"REFUND_ONLY\",\"reason\":\"质量问题\",\"memberId\":999,\"amount\":\"0.01\"}"))
                .andExpect(status().isOk());
        verify(create).executeForMember(any(), eq(1L));
    }
}
