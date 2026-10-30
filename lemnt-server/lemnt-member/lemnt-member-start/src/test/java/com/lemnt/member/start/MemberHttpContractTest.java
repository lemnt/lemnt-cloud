package com.lemnt.member.start;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.domain.model.PageResult;
import com.lemnt.common.security.constant.SecurityConstants;
import com.lemnt.common.security.handler.GlobalExceptionHandler;
import com.lemnt.common.security.interceptor.HeaderInterceptor;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.member.adapter.app.address.AppMemberAddressController;
import com.lemnt.member.adapter.app.growth.AppMemberExperienceRecordController;
import com.lemnt.member.adapter.app.growth.AppMemberPointRecordController;
import com.lemnt.member.adapter.app.growth.AppMemberSignRecordController;
import com.lemnt.member.application.address.executor.MemberAddressDefaultQueryExe;
import com.lemnt.member.application.address.executor.MemberAddressDeleteCmdExe;
import com.lemnt.member.application.address.executor.MemberAddressDetailQueryExe;
import com.lemnt.member.application.address.executor.MemberAddressListQueryExe;
import com.lemnt.member.application.address.executor.MemberAddressSaveCmdExe;
import com.lemnt.member.application.address.qry.AddressListQry;
import com.lemnt.member.application.growth.executor.MemberAssetRecordPageQueryExe;
import com.lemnt.member.application.growth.executor.MemberSignCreateCmdExe;
import com.lemnt.member.application.growth.executor.MemberSignRecordPageQueryExe;
import com.lemnt.member.application.growth.executor.MemberSignSummaryQueryExe;
import com.lemnt.member.application.growth.qry.AssetRecordPageQry;
import com.lemnt.member.application.growth.qry.SignRecordPageQry;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberHttpContractTest {
    @Test
    void accountIdentityComesFromGatewayContextWithoutTokenAuthentication() throws Exception {
        var delete = mock(MemberAddressDeleteCmdExe.class);
        var save = mock(MemberAddressSaveCmdExe.class);
        var detail = mock(MemberAddressDetailQueryExe.class);
        var queries = mock(MemberAddressListQueryExe.class);
        var defaults = mock(MemberAddressDefaultQueryExe.class);
        when(queries.execute(new AddressListQry(7L))).thenReturn(List.of());
        var mvc = MockMvcBuilders.standaloneSetup(
                new AppMemberAddressController(delete, detail, queries, save, defaults))
                .addInterceptors(new HeaderInterceptor())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        mvc.perform(get("/app/addresses")
                .header(SecurityConstants.HEADER_USER_ID, "7")
                .header("memberId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
        verify(queries).execute(new AddressListQry(7L));
        assertEquals(0L, SecurityUtils.getUserId(), "请求结束必须清理会员身份上下文");
        verifyNoInteractions(delete, save, detail, defaults);
    }

    @Test
    void businessFailuresUseGlobalProblemResponse() throws Exception {
        var queries = mock(MemberAddressListQueryExe.class);
        var mvc = MockMvcBuilders.standaloneSetup(new AppMemberAddressController(
                mock(MemberAddressDeleteCmdExe.class), mock(MemberAddressDetailQueryExe.class),
                queries, mock(MemberAddressSaveCmdExe.class), mock(MemberAddressDefaultQueryExe.class)))
                .addInterceptors(new HeaderInterceptor())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        for (int code : new int[] {400, 409}) {
            doThrow(new ServiceException("会员业务校验失败", code))
                    .when(queries).execute(new AddressListQry(7L));
            mvc.perform(get("/app/addresses").header(SecurityConstants.HEADER_USER_ID, "7"))
                    .andExpect(status().is(code))
                    .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                    .andExpect(jsonPath("$.status").value(code))
                    .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"))
                    .andExpect(jsonPath("$.detail").value("会员业务校验失败"));
        }
    }
    @Test
    void recordPageInputsKeepIndependentDefaultsValidationAndAssetTypes() throws Exception {
        var assets = mock(MemberAssetRecordPageQueryExe.class);
        var signs = mock(MemberSignRecordPageQueryExe.class);
        when(assets.execute(new AssetRecordPageQry(7L, "POINT", 1, 20)))
                .thenReturn(new PageResult<>(List.of(), 0));
        when(assets.execute(new AssetRecordPageQry(7L, "EXPERIENCE", 3, 25)))
                .thenReturn(new PageResult<>(List.of(), 0));
        when(signs.execute(new SignRecordPageQry(7L, 2, 15)))
                .thenReturn(new PageResult<>(List.of(), 0));
        var mvc = MockMvcBuilders.standaloneSetup(new AppMemberSignRecordController(
                mock(MemberSignCreateCmdExe.class), mock(MemberSignSummaryQueryExe.class), signs),
                new AppMemberPointRecordController(assets), new AppMemberExperienceRecordController(assets))
                .addInterceptors(new HeaderInterceptor())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        for (String uri : new String[] {
                "/app/growths/signin/records?pageNum=2&pageSize=15",
                "/app/growths/point-records",
                "/app/growths/experience-records?pageNum=3&pageSize=25"}) {
            mvc.perform(get(uri).header(SecurityConstants.HEADER_USER_ID, "7"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rows").isArray());
        }
        for (String path : new String[] {"signin/records", "point-records", "experience-records"}) {
            for (String query : new String[] {"?pageSize=101", "?pageNum=0"}) {
                mvc.perform(get("/app/growths/" + path + query).header(SecurityConstants.HEADER_USER_ID, "7"))
                        .andExpect(status().isUnprocessableEntity());
            }
        }
        verify(assets).execute(new AssetRecordPageQry(7L, "POINT", 1, 20));
        verify(assets).execute(new AssetRecordPageQry(7L, "EXPERIENCE", 3, 25));
        verify(signs).execute(new SignRecordPageQry(7L, 2, 15));
        verifyNoMoreInteractions(assets, signs);
    }
    /** false必须绑定为支出，外部memberId不能替换网关会员身份。 */
    @Test
    void appPointFiltersKeepFalseAndTrustedMemberId() throws Exception {
        var assets = mock(MemberAssetRecordPageQueryExe.class);
        var start = java.time.LocalDateTime.of(2026, 10, 30, 10, 0);
        var expected = new AssetRecordPageQry(7L, "POINT", 1, 20, false, start, start.plusHours(1));
        when(assets.execute(expected)).thenReturn(new PageResult<>(List.of(), 0));
        var mvc = MockMvcBuilders.standaloneSetup(new AppMemberPointRecordController(assets))
                .addInterceptors(new HeaderInterceptor()).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/app/growths/point-records").header(SecurityConstants.HEADER_USER_ID, "7")
                .param("memberId", "999").param("addStatus", "false")
                .param("startTime", "2026-10-30 10:00:00").param("endTime", "2026-10-30 11:00:00"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.rows").isArray());
        verify(assets).execute(expected);
        assertEquals(0L, SecurityUtils.getUserId());
    }
}
