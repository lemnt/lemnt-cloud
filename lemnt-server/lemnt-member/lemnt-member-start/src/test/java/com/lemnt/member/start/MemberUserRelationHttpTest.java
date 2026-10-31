package com.lemnt.member.start;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.interceptor.HeaderInterceptor;
import com.lemnt.member.adapter.web.user.MemberUserController;
import com.lemnt.member.adapter.app.user.AppMemberUserController;
import com.lemnt.member.application.user.dto.MemberDTO;
import com.lemnt.member.application.user.converter.MemberUserDTOConverter;
import com.lemnt.member.application.user.executor.*;
import com.lemnt.member.application.user.qry.MemberDetailQry;
import com.lemnt.member.application.user.qry.MemberPageQry;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberUserRelationHttpTest {
    /** 后台关系字段平铺，App即使接到同一DTO也不暴露后台新增字段。 */
    @Test
    void webRelationsStayFlatAndAreNotAddedToAppResponse() throws Exception {
        var base = new MemberDTO(1L, "member", null, null, null, 1, 0L, 0L, 7L, 8L, null, null, null);
        var value = MemberUserDTOConverter.withRelations(base, List.of(9L), List.of("Active"), "Gold", "VIP");
        var detail = mock(MemberUserDetailQueryExe.class);
        var page = mock(MemberUserPageQueryExe.class);
        var app = mock(MemberDetailQueryExe.class);
        when(detail.execute(new MemberDetailQry(1L))).thenReturn(value);
        when(app.execute(new MemberDetailQry(1L))).thenReturn(value);
        when(page.execute(new MemberPageQry(null, 1, 20))).thenReturn(new PageResult<>(List.of(value), 1));
        var mvc = MockMvcBuilders.standaloneSetup(new MemberUserController(detail, mock(MemberUserUpdateCmdExe.class), page),
                new AppMemberUserController(app, mock(MemberUpdateCmdExe.class)))
                .addInterceptors(new HeaderInterceptor()).build();
        mvc.perform(get("/member-users/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tagIds[0]").value(9))
                .andExpect(jsonPath("$.data.levelName").value("Gold"))
                .andExpect(jsonPath("$.data.groupName").value("VIP"))
                .andExpect(jsonPath("$.data.profile").doesNotExist());
        mvc.perform(get("/member-users")).andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].tagNames[0]").value("Active"))
                .andExpect(jsonPath("$.total").value(1));
        mvc.perform(get("/app/users/user").header(SecurityConstants.HEADER_USER_ID, "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.tagIds").doesNotExist())
                .andExpect(jsonPath("$.data.tagNames").doesNotExist())
                .andExpect(jsonPath("$.data.levelName").doesNotExist())
                .andExpect(jsonPath("$.data.groupName").doesNotExist());
    }
}
