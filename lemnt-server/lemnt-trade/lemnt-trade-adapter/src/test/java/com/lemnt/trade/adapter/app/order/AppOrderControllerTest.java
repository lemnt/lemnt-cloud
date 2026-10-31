package com.lemnt.trade.adapter.app.order;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.trade.application.fulfillment.dto.MemberOrderDetailDTO;
import com.lemnt.trade.application.fulfillment.dto.MemberOrderStatusCountDTO;
import com.lemnt.trade.application.fulfillment.executor.MemberOrderDetailQueryExe;
import com.lemnt.trade.application.fulfillment.executor.MemberOrderPageQueryExe;
import com.lemnt.trade.application.fulfillment.executor.MemberOrderStatusCountQueryExe;
import com.lemnt.trade.application.fulfillment.executor.MemberOrderTrackQueryExe;
import com.lemnt.trade.application.fulfillment.executor.OrderReceiveCmdExe;
import com.lemnt.trade.application.fulfillment.qry.MemberOrderPageQry;
import com.lemnt.trade.application.order.executor.TradeOrderCloseCmdExe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 订单中心只取认证上下文中的会员编号，不接收Body或伪造身份头。 */
class AppOrderControllerTest {
    @AfterEach
    void cleanup() { SecurityContextHolder.remove(); }

    private AppOrderController controller(TradeOrderCloseCmdExe close, OrderReceiveCmdExe receive,
            MemberOrderPageQueryExe page, MemberOrderDetailQueryExe detail, MemberOrderTrackQueryExe track,
            MemberOrderStatusCountQueryExe count) {
        return new AppOrderController(close, receive, page, detail, track, count);
    }

    @Test
    void cancelAndReceiveUseVerifiedMemberContext() throws Exception {
        SecurityContextHolder.setUserId("1");
        var close = mock(TradeOrderCloseCmdExe.class);
        var receive = mock(OrderReceiveCmdExe.class);
        when(close.executeForMember(1L, "TR1")).thenReturn(true);
        var mvc = MockMvcBuilders.standaloneSetup(controller(close, receive,
                mock(MemberOrderPageQueryExe.class), mock(MemberOrderDetailQueryExe.class),
                mock(MemberOrderTrackQueryExe.class), mock(MemberOrderStatusCountQueryExe.class))).build();
        mvc.perform(post("/trade/app/orders/TR1/cancel").header("X-User-Id", "999")
                .contentType(MediaType.APPLICATION_JSON).content("{\"memberId\":999,\"userType\":2}"))
                .andExpect(status().isOk());
        mvc.perform(post("/trade/app/orders/TR1/receive").header("X-User-Id", "999"))
                .andExpect(status().isOk());
        verify(close).executeForMember(1L, "TR1");
        verify(receive).executeForMember(1L, "TR1");
        verifyNoMoreInteractions(close, receive);
    }

    @Test
    void pageUsesVerifiedMemberAndIgnoresForgedMemberFilter() throws Exception {
        SecurityContextHolder.setUserId("1");
        var page = mock(MemberOrderPageQueryExe.class);
        when(page.execute(any(), any())).thenReturn(PageResult.of(List.of(), 0L));
        var mvc = MockMvcBuilders.standaloneSetup(controller(mock(TradeOrderCloseCmdExe.class),
                mock(OrderReceiveCmdExe.class), page, mock(MemberOrderDetailQueryExe.class),
                mock(MemberOrderTrackQueryExe.class), mock(MemberOrderStatusCountQueryExe.class))).build();
        mvc.perform(get("/trade/app/orders?status=PAID&memberId=999").header("X-User-Id", "999"))
                .andExpect(status().isOk());
        ArgumentCaptor<MemberOrderPageQry> captor = ArgumentCaptor.forClass(MemberOrderPageQry.class);
        verify(page).execute(eq(1L), captor.capture());
        assertEquals("PAID", captor.getValue().status());
    }

    @Test
    void detailReturnsNotFoundForOtherMemberAndSuccessForOwner() throws Exception {
        SecurityContextHolder.setUserId("1");
        var detail = mock(MemberOrderDetailQueryExe.class);
        var mvc = MockMvcBuilders.standaloneSetup(controller(mock(TradeOrderCloseCmdExe.class),
                mock(OrderReceiveCmdExe.class), mock(MemberOrderPageQueryExe.class), detail,
                mock(MemberOrderTrackQueryExe.class), mock(MemberOrderStatusCountQueryExe.class))).build();
        when(detail.execute(1L, "TR1")).thenReturn(null);
        mvc.perform(get("/trade/app/orders/TR1").header("X-User-Id", "999"))
                .andExpect(status().isOk());
        var value = new MemberOrderDetailDTO("TR1", "PAID", new BigDecimal("10.00"), LocalDateTime.now(),
                false, List.of(), null, false, null);
        when(detail.execute(1L, "TR1")).thenReturn(value);
        mvc.perform(get("/trade/app/orders/TR1")).andExpect(status().isOk());
        verify(detail, times(2)).execute(1L, "TR1");
    }

    @Test
    void statusCountsAndTracksUseVerifiedMember() throws Exception {
        SecurityContextHolder.setUserId("1");
        var count = mock(MemberOrderStatusCountQueryExe.class);
        when(count.execute(1L)).thenReturn(new MemberOrderStatusCountDTO(1, 2, 3, 4, 5, 6, 7));
        var tracks = mock(MemberOrderTrackQueryExe.class);
        when(tracks.execute(1L, "TR1")).thenReturn(List.of());
        var mvc = MockMvcBuilders.standaloneSetup(controller(mock(TradeOrderCloseCmdExe.class),
                mock(OrderReceiveCmdExe.class), mock(MemberOrderPageQueryExe.class),
                mock(MemberOrderDetailQueryExe.class), tracks, count)).build();
        mvc.perform(get("/trade/app/orders/status-counts")).andExpect(status().isOk());
        mvc.perform(get("/trade/app/orders/TR1/tracks")).andExpect(status().isOk());
        verify(count).execute(1L);
        verify(tracks).execute(1L, "TR1");
    }
}
