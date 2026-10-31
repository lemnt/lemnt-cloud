package com.lemnt.system.start.area;
import com.lemnt.system.infrastructure.area.gateway.AreaGatewayImpl;
import com.lemnt.system.application.area.executor.AreaTreeQueryExe;
import com.lemnt.system.adapter.web.area.AreaController;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
/** 读取迁移的实际源资源，不用虚拟地区代替省市区数据。 */
class AreaTreeTest {
    @Test
    void loadsSourceTreeAndKeepsImmutableProvinceCityDistrictPath() {
        var gateway=new AreaGatewayImpl();
        var beijing=gateway.chinaTree().stream().filter(v->v.id()==110000).findFirst().orElseThrow();
        assertEquals("北京市",beijing.name());
        var city=beijing.children().stream().filter(v->v.id()==110100).findFirst().orElseThrow();
        assertEquals("东城区",city.children().stream().filter(v->v.id()==110101).findFirst().orElseThrow().name());
        assertThrows(UnsupportedOperationException.class,()->gateway.chinaTree().clear());
        assertThrows(UnsupportedOperationException.class,()->city.children().clear());
        assertFalse(gateway.chinaTree().stream().anyMatch(v->v.id()==2));
    }
    @Test
    void mapsActualTreeThroughApplicationAndHttpResponse() throws Exception {
        var mvc=MockMvcBuilders.standaloneSetup(new AreaController(new AreaTreeQueryExe(new AreaGatewayImpl()))).build();
        mvc.perform(get("/areas/tree")).andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[?(@.id == 110000)].name").value("北京市"));
        assertNotNull(AreaController.class.getMethod("tree").getAnnotation(com.lemnt.common.security.annotation.RequiresLogin.class));
    }
    /** 地区名称复用实际CSV，国内不加中国，国外保留国家，未知不推断。 */
    @Test
    void remoteNamesFormatSourcePathsAndHandleUnknownIds() {
        var remote = new com.lemnt.system.application.dubbo.RemoteAreaServiceImpl(new AreaGatewayImpl());
        var names = remote.getNames(java.util.List.of(110101L, 110100L, 110000L, 1L, 2L, Long.MAX_VALUE, 110101L));
        assertEquals("北京市 北京市 东城区", names.get(110101L));
        assertEquals("北京市 北京市", names.get(110100L));
        assertEquals("北京市", names.get(110000L));
        assertEquals("中国", names.get(1L));
        assertEquals("蒙古", names.get(2L));
        assertFalse(names.containsKey(Long.MAX_VALUE));
        assertEquals(5, names.size());
        assertTrue(remote.getNames(java.util.List.of()).isEmpty());
        assertTrue(remote.getNames(null).isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> names.clear());
    }

}
