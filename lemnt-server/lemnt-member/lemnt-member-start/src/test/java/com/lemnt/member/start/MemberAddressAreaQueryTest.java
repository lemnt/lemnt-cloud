package com.lemnt.member.start;

import com.lemnt.member.application.address.dto.AddressDTO;
import com.lemnt.member.adapter.app.address.AppMemberAddressController;
import com.lemnt.member.application.address.executor.MemberAddressDeleteCmdExe;
import com.lemnt.member.application.address.executor.MemberAddressSaveCmdExe;
import com.lemnt.common.security.interceptor.HeaderInterceptor;
import com.lemnt.common.security.constant.SecurityConstants;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.lemnt.member.application.address.service.MemberAddressQueryService;
import com.lemnt.member.application.address.executor.MemberAddressListQueryExe;
import com.lemnt.member.application.address.executor.MemberAddressDetailQueryExe;
import com.lemnt.member.application.address.executor.MemberAddressDefaultQueryExe;
import com.lemnt.member.application.address.qry.AddressListQry;
import com.lemnt.member.application.address.qry.AddressDetailQry;
import com.lemnt.member.application.address.qry.AddressDefaultQry;
import com.lemnt.member.domain.address.gateway.MemberAddressGateway;
import com.lemnt.member.domain.address.gateway.MemberAddressAreaGateway;
import com.lemnt.member.domain.address.model.Address;
import com.lemnt.member.infrastructure.address.gateway.MemberAddressAreaGatewayImpl;
import com.lemnt.system.api.RemoteAreaService;
import org.apache.dubbo.rpc.RpcException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberAddressAreaQueryTest {
    private Address row(long id, long area) {
        return new Address(id, 7L, "收件人", "13800000003", area, "street", true);
    }

    /** 同地区地址去重批量返显一次，原地址保持不变，未知地区仍保留真实编号。 */
    @Test
    void listResolvesDistinctAreaIdsOnceWithoutChangingAddresses() {
        var areas = mock(MemberAddressAreaGateway.class);
        var addresses = mock(MemberAddressGateway.class);
        var first = row(1, 110101);
        when(addresses.addresses(7L)).thenReturn(List.of(first, row(2, 110101), row(3, Long.MAX_VALUE)));
        when(areas.names(List.of(110101L, Long.MAX_VALUE))).thenReturn(Map.of(110101L, "北京市 北京市 东城区"));
        var query = new MemberAddressListQueryExe(addresses, new MemberAddressQueryService(areas));
        var result = query.execute(new AddressListQry(7L));
        assertEquals("北京市 北京市 东城区", result.get(0).areaName());
        assertEquals(result.get(0).areaName(), result.get(1).areaName());
        assertNull(result.get(2).areaName());
        assertEquals(Long.MAX_VALUE, result.get(2).areaId());
        assertEquals(first.mobile(), result.get(0).mobile());
        assertEquals(first.memberId(), result.get(0).memberId());
        verify(areas).names(List.of(110101L, Long.MAX_VALUE));
        verifyNoMoreInteractions(areas);
    }

    /** 空列表、缺失/非本人地址不触发地区查询，详情和默认统一名称投影。 */
    @Test
    void detailAndDefaultEnrichOnlyExistingOwnedAddress() {
        var areas = mock(MemberAddressAreaGateway.class);
        var addresses = mock(MemberAddressGateway.class);
        var enrichment = new MemberAddressQueryService(areas);
        var detail = new MemberAddressDetailQueryExe(addresses, enrichment);
        var defaults = new MemberAddressDefaultQueryExe(addresses, enrichment);
        assertTrue(enrichment.enrich(List.of()).isEmpty());
        assertNull(detail.execute(new AddressDetailQry(7L, 1L)));
        assertNull(defaults.execute(new AddressDefaultQry(7L)));
        verifyNoInteractions(areas);
        when(addresses.address(7L, 1L)).thenReturn(row(1, 110101));
        when(addresses.defaultAddress(7L)).thenReturn(row(1, 110101));
        when(areas.names(List.of(110101L))).thenReturn(Map.of(110101L, "北京市 北京市 东城区"));
        assertEquals(detail.execute(new AddressDetailQry(7L, 1L)), defaults.execute(new AddressDefaultQry(7L)));
    }

    /** 地区RPC不可用时地址显示降级为未知；空批次不调用RPC。 */
    @Test
    void unavailableRegionServiceDoesNotHideAddressesOrInventNames() {
        var remote = mock(RemoteAreaService.class);
        var gateway = new MemberAddressAreaGatewayImpl();
        ReflectionTestUtils.setField(gateway, "remoteAreaService", remote);
        assertTrue(gateway.names(List.of()).isEmpty());
        verifyNoInteractions(remote);
        when(remote.getNames(List.of(110101L))).thenThrow(new RpcException("地区服务暂不可用"));
        var result = new MemberAddressQueryService(gateway).enrich(row(1, 110101));
        assertEquals(1L, result.id());
        assertEquals(110101L, result.areaId());
        assertNull(result.areaName());
    }
    /** 列表、详情、默认独立响应平铺返回名称，不改变原地址结构。 */
    @Test
    void readHttpResponsesExposeAreaNameFlatly() throws Exception {
        var lists = mock(MemberAddressListQueryExe.class);
        var detail = mock(MemberAddressDetailQueryExe.class);
        var defaults = mock(MemberAddressDefaultQueryExe.class);
        var value = new AddressDTO(1L, 7L, "收件人",
                "13800000003", 110101L, "street", true, "北京市 北京市 东城区");
        when(lists.execute(new AddressListQry(7L))).thenReturn(List.of(value));
        when(detail.execute(new AddressDetailQry(7L, 1L))).thenReturn(value);
        when(defaults.execute(new AddressDefaultQry(7L))).thenReturn(value);
        var controller = new AppMemberAddressController(
                mock(MemberAddressDeleteCmdExe.class), detail, lists,
                mock(MemberAddressSaveCmdExe.class), defaults);
        var mvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(new HeaderInterceptor()).build();
        mvc.perform(get("/app/addresses")
                .header(SecurityConstants.HEADER_USER_ID, "7"))
                .andExpect(jsonPath("$.data[0].areaName").value(value.areaName()));
        for (String path : List.of("/app/addresses/1", "/app/addresses/default")) {
            mvc.perform(get(path)
                    .header(SecurityConstants.HEADER_USER_ID, "7"))
                    .andExpect(jsonPath("$.data.areaName").value(value.areaName()))
                    .andExpect(jsonPath("$.data.areaId").value(110101))
                    .andExpect(jsonPath("$.data.address").doesNotExist());
        }
    }

}
