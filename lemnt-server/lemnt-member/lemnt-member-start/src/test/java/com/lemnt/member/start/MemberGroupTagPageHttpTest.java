package com.lemnt.member.start;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.security.handler.GlobalExceptionHandler;
import com.lemnt.member.adapter.web.user.MemberGroupQueryController;
import com.lemnt.member.adapter.web.user.MemberTagQueryController;
import com.lemnt.member.application.user.executor.MemberGroupPageQueryExe;
import com.lemnt.member.application.user.executor.MemberTagPageQueryExe;
import com.lemnt.member.application.user.qry.GroupPageQry;
import com.lemnt.member.application.user.qry.TagPageQry;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDateTime;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberGroupTagPageHttpTest {
    /** 独立分页请求各自绑定，标签不接收状态和会员ID。 */
    @Test
    void bindsIndependentGroupAndTagPageFilters() throws Exception {
        var groups = mock(MemberGroupPageQueryExe.class);
        var tags = mock(MemberTagPageQueryExe.class);
        var start = LocalDateTime.of(2026, 10, 30, 10, 0);
        var end = start.plusHours(1);
        var groupQry = new GroupPageQry("VIP", 0, 1, 20, start, end);
        var tagQry = new TagPageQry("active", 1, 20, start, end);
        when(groups.execute(groupQry)).thenReturn(new PageResult<>(List.of(), 0));
        when(tags.execute(tagQry)).thenReturn(new PageResult<>(List.of(), 0));
        var mvc = MockMvcBuilders.standaloneSetup(new MemberGroupQueryController(groups), new MemberTagQueryController(tags))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/groups/page").param("name", "VIP").param("status", "0")
                .param("startTime", "2026-10-30 10:00:00").param("endTime", "2026-10-30 11:00:00"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rows").isArray());
        mvc.perform(get("/tags/page").param("name", "active").param("status", "0").param("memberId", "999")
                .param("startTime", "2026-10-30 10:00:00").param("endTime", "2026-10-30 11:00:00"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rows").isArray());
        mvc.perform(get("/groups/page").param("status", "2"))
                .andExpect(status().isUnprocessableEntity());
        verify(groups).execute(groupQry);
        verify(tags).execute(tagQry);
        verifyNoMoreInteractions(groups, tags);
    }
}
