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
}
