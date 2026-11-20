package com.lemnt.member.start;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.security.handler.GlobalExceptionHandler;
import com.lemnt.member.adapter.web.user.MemberStatisticsController;
import com.lemnt.member.adapter.web.user.MemberUserController;
import com.lemnt.member.application.user.dto.MemberStatisticsDTO;
import com.lemnt.member.application.user.executor.MemberStatisticsQueryExe;
import com.lemnt.member.application.user.executor.MemberUserDetailQueryExe;
import com.lemnt.member.application.user.executor.MemberUserPageQueryExe;
import com.lemnt.member.application.user.executor.MemberUserUpdateCmdExe;
import com.lemnt.member.application.user.qry.MemberPageQry;
import com.lemnt.member.application.user.qry.MemberStatisticsQry;
import com.lemnt.member.domain.user.model.MemberStatistics.Distribution;
import com.lemnt.member.domain.user.model.MemberStatistics.Registration;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberUserFilterHttpTest {
    /** 日期参数以自然日绑定，响应保留空地区及无新增日期。 */
    @Test
    void bindsStatisticsPeriodAndWritesCalendarDates() throws Exception {
        var query = mock(MemberStatisticsQueryExe.class);
        var date = LocalDate.of(2026, 11, 18);
        var expected = new MemberStatisticsQry(date, date);
        when(query.execute(expected))
                .thenReturn(
                        new MemberStatisticsDTO(
                                List.of(new Registration(date, 0)),
                                List.of(new Distribution(0, 1)),
                                List.of(new Distribution(null, 1))));
        var mvc =
                MockMvcBuilders.standaloneSetup(new MemberStatisticsController(query))
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
        mvc.perform(
                        get("/member-users/statistics")
                                .param("from", date.toString())
                                .param("to", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.registrations[0].date").value("2026-11-18"))
                .andExpect(jsonPath("$.data.registrations[0].count").value(0))
                .andExpect(jsonPath("$.data.sexes[0].value").value(0))
                .andExpect(jsonPath("$.data.areas[0].count").value(1));
        mvc.perform(get("/member-users/statistics").param("from", "2026-11-18"))
                .andExpect(status().isUnprocessableEntity());
        verify(query).execute(expected);
        verifyNoMoreInteractions(query);
    }

    /**
     * 独立请求绑定重复标签键、两种日期和归属条件。
     */
    @Test
    void bindsAllUserFilterFieldsAndRepeatedTagKeys() throws Exception {
        var query = mock(MemberUserPageQueryExe.class);
        var start = LocalDateTime.of(2026, 10, 30, 10, 0);
        var end = start.plusHours(1);
        var expected =
                new MemberPageQry(
                        "138",
                        "shop",
                        "会员",
                        7L,
                        8L,
                        List.of(9L, 10L),
                        start,
                        end,
                        start,
                        end,
                        2,
                        20);
        when(query.execute(expected)).thenReturn(new PageResult<>(List.of(), 0));
        var mvc =
                MockMvcBuilders.standaloneSetup(
                                new MemberUserController(
                                        mock(MemberUserDetailQueryExe.class),
                                        mock(MemberUserUpdateCmdExe.class),
                                        query))
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
        mvc.perform(
                        get("/member-users")
                                .param("mobile", "138")
                                .param("email", "shop")
                                .param("nickname", "会员")
                                .param("levelId", "7")
                                .param("groupId", "8")
                                .param("tagIds", "9", "10")
                                .param("pageNum", "2")
                                .param("loginStartTime", "2026-10-30 10:00:00")
                                .param("loginEndTime", "2026-10-30 11:00:00")
                                .param("createStartTime", "2026-10-30 10:00:00")
                                .param("createEndTime", "2026-10-30 11:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows").isArray());
        verify(query).execute(expected);
    }

    /**
     * 非正关联ID及非法分页不进入查询。
     */
    @Test
    void validatesUserAssociationIdsAndPagination() throws Exception {
        var query = mock(MemberUserPageQueryExe.class);
        var mvc =
                MockMvcBuilders.standaloneSetup(
                                new MemberUserController(
                                        mock(MemberUserDetailQueryExe.class),
                                        mock(MemberUserUpdateCmdExe.class),
                                        query))
                        .setControllerAdvice(new GlobalExceptionHandler())
                        .build();
        mvc.perform(get("/member-users").param("levelId", "0"))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/member-users").param("tagIds", "0"))
                .andExpect(status().isUnprocessableEntity());
        mvc.perform(get("/member-users").param("pageSize", "101"))
                .andExpect(status().isUnprocessableEntity());
        verifyNoInteractions(query);
    }
}
