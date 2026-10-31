package com.lemnt.member.start;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.security.handler.GlobalExceptionHandler;
import com.lemnt.member.adapter.web.growth.MemberLevelRecordController;
import com.lemnt.member.application.growth.executor.MemberLevelRecordAllPageQueryExe;
import com.lemnt.member.application.growth.executor.MemberLevelRecordDetailQueryExe;
import com.lemnt.member.application.growth.qry.LevelRecordAllPageQry;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberLevelRecordHttpTest {
    /** 等级编号和时间范围绑定专用Req，并保持统一分页返回。 */
    @Test
    void bindsLevelAndTimeRangeToIndependentQuery() throws Exception {
        var query = mock(MemberLevelRecordAllPageQueryExe.class);
        var expected = new LevelRecordAllPageQry(7L, 2, 1, 9L,
                LocalDateTime.of(2026, 10, 30, 10, 0), LocalDateTime.of(2026, 10, 30, 11, 0));
        when(query.execute(expected)).thenReturn(new PageResult<>(List.of(), 0));
        var mvc = MockMvcBuilders.standaloneSetup(new MemberLevelRecordController(
                query, mock(MemberLevelRecordDetailQueryExe.class))).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/member/admin/level-records").param("memberId", "7").param("levelId", "9")
                .param("pageNum", "2").param("pageSize", "1")
                .param("startTime", "2026-10-30 10:00:00").param("endTime", "2026-10-30 11:00:00"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.total").value(0));
        verify(query).execute(expected);
    }

    /** 非正等级编号与非法分页在适配层拒绝，不进入存储查询。 */
    @Test
    void validatesLevelIdAndPaginationBeforeQuery() throws Exception {
        var query = mock(MemberLevelRecordAllPageQueryExe.class);
        var mvc = MockMvcBuilders.standaloneSetup(new MemberLevelRecordController(
                query, mock(MemberLevelRecordDetailQueryExe.class))).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/member/admin/level-records").param("levelId", "0"))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/member/admin/level-records").param("pageSize", "101"))
                .andExpect(status().isUnprocessableEntity());
        verifyNoInteractions(query);
    }
}
