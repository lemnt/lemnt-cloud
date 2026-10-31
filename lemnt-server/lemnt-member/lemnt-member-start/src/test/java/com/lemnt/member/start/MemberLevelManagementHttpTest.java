package com.lemnt.member.start;

import com.lemnt.common.security.handler.GlobalExceptionHandler;
import com.lemnt.member.adapter.web.growth.MemberLevelController;
import com.lemnt.member.application.growth.executor.*;
import com.lemnt.member.application.growth.qry.LevelListQry;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberLevelManagementHttpTest {
    /** 后台独立列表请求保留状态0，简单列表不能被请求状态改成停用。 */
    @Test
    void levelNameAndStatusBindIndependentlyFromSimpleList() throws Exception {
        var list = mock(MemberLevelListQueryExe.class);
        when(list.execute(new LevelListQry(false, "Gold", 0))).thenReturn(List.of());
        when(list.execute(new LevelListQry(true))).thenReturn(List.of());
        var mvc = MockMvcBuilders.standaloneSetup(new MemberLevelController(mock(MemberLevelDeleteCmdExe.class),
                mock(MemberLevelDetailQueryExe.class), mock(MemberLevelSaveCmdExe.class), list))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/growth/levels").param("name", "Gold").param("status", "0"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/growth/levels/list-all-simple").param("status", "0"))
                .andExpect(status().isOk());
        mvc.perform(get("/growth/levels").param("status", "2"))
                .andExpect(status().isUnprocessableEntity());
        verify(list).execute(new LevelListQry(false, "Gold", 0));
        verify(list).execute(new LevelListQry(true));
        verifyNoMoreInteractions(list);
    }
}
