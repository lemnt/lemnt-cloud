package com.lemnt.member.start;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.security.handler.GlobalExceptionHandler;
import com.lemnt.member.adapter.web.growth.MemberExperienceRecordController;
import com.lemnt.member.application.growth.executor.MemberExperienceRecordAllPageQueryExe;
import com.lemnt.member.application.growth.executor.MemberAssetRecordDetailQueryExe;
import com.lemnt.member.application.growth.qry.ExperienceRecordAllPageQry;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberExperienceRecordHttpTest {
    /** 真实HTTP绑定独立筛选，0不丢失，日期按本地数据库时间传递。 */
    @Test
    void bindsIndependentExperienceFiltersAndPagination() throws Exception {
        var query = mock(MemberExperienceRecordAllPageQueryExe.class);
        var expected = new ExperienceRecordAllPageQry(7L, 2, 1, "E1", 0, "管理员调整",
                LocalDateTime.of(2026, 10, 30, 10, 0), LocalDateTime.of(2026, 10, 30, 11, 0));
        when(query.execute(expected)).thenReturn(new PageResult<>(List.of(), 0));
        var mvc = MockMvcBuilders.standaloneSetup(new MemberExperienceRecordController(
                query, mock(MemberAssetRecordDetailQueryExe.class))).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/member/admin/experience-records").param("memberId", "7")
                .param("pageNum", "2").param("pageSize", "1").param("bizNo", "E1")
                .param("experienceBizType", "0").param("title", "管理员调整")
                .param("startTime", "2026-10-30 10:00:00").param("endTime", "2026-10-30 11:00:00"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.total").value(0));
        verify(query).execute(expected);
    }

    /** 非法分页和过长字段在适配层校验，不进入应用查询。 */
    @Test
    void validatesExperienceRequestBeforeQuerying() throws Exception {
        var query = mock(MemberExperienceRecordAllPageQueryExe.class);
        var mvc = MockMvcBuilders.standaloneSetup(new MemberExperienceRecordController(
                query, mock(MemberAssetRecordDetailQueryExe.class))).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/member/admin/experience-records").param("pageSize", "101"))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/member/admin/experience-records").param("bizNo", "x".repeat(65)))
                .andExpect(status().isUnprocessableEntity());
        verifyNoInteractions(query);
    }
}
