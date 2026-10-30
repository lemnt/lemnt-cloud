package com.lemnt.member.start;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.security.handler.GlobalExceptionHandler;
import com.lemnt.member.adapter.web.growth.MemberSignRecordController;
import com.lemnt.member.application.growth.executor.MemberSignRecordAllPageQueryExe;
import com.lemnt.member.application.growth.qry.SignRecordAllPageQry;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberSignRecordHttpTest {
    /** 天数和真实时间绑定独立Req，非正天数不进入应用查询。 */
    @Test
    void bindsAndValidatesSignDayAndEventTime() throws Exception {
        var query = mock(MemberSignRecordAllPageQueryExe.class);
        var expected = new SignRecordAllPageQry(7L, 1, 20, "会员", 2,
                LocalDateTime.of(2026, 10, 29, 10, 0), LocalDateTime.of(2026, 10, 30, 11, 0));
        when(query.execute(expected)).thenReturn(new PageResult<>(List.of(), 0));
        var mvc = MockMvcBuilders.standaloneSetup(new MemberSignRecordController(query))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/member/admin/signin-records").param("memberId", "7").param("nickname", "会员")
                .param("day", "2").param("startTime", "2026-10-29 10:00:00").param("endTime", "2026-10-30 11:00:00"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rows").isArray());
        mvc.perform(get("/member/admin/signin-records").param("day", "0"))
                .andExpect(status().isUnprocessableEntity());
        verify(query).execute(expected);
        verifyNoMoreInteractions(query);
    }
}
