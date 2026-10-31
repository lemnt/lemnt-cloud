package com.lemnt.member.start;

import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.security.handler.GlobalExceptionHandler;
import com.lemnt.member.adapter.web.growth.MemberPointRecordController;
import com.lemnt.member.application.growth.executor.MemberAssetRecordAllPageQueryExe;
import com.lemnt.member.application.growth.executor.MemberAssetRecordDetailQueryExe;
import com.lemnt.member.application.growth.qry.AssetRecordAllPageQry;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberPointRecordHttpTest {
    /** 标题与原有条件独立绑定，过长标题不进入查询。 */
    @Test
    void bindsAndValidatesPointTitleWithExistingFilters() throws Exception {
        var query = mock(MemberAssetRecordAllPageQueryExe.class);
        var expected = new AssetRecordAllPageQry("POINT", 7L, 1, 20, "会员", "ADMIN", 2, "管理");
        when(query.execute(expected)).thenReturn(new PageResult<>(List.of(), 0));
        var mvc = MockMvcBuilders.standaloneSetup(new MemberPointRecordController(
                query, mock(MemberAssetRecordDetailQueryExe.class))).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/member/admin/point-records").param("memberId", "7").param("nickname", "会员")
                .param("bizType", "ADMIN").param("pointBizType", "2").param("title", "管理"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rows").isArray());
        mvc.perform(get("/member/admin/point-records").param("title", "x".repeat(256)))
                .andExpect(status().isUnprocessableEntity());
        verify(query).execute(expected);
        verifyNoMoreInteractions(query);
    }
}
