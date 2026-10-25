package com.lemnt.mall.adapter.web.catalog;

import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.application.catalog.executor.*;
import com.lemnt.mall.domain.catalog.gateway.CatalogGateway;
import com.lemnt.mall.domain.catalog.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CatalogHttpContractTest {
    CatalogGateway gateway = mock(CatalogGateway.class);
    SpuCreateCmdExe create = mock(SpuCreateCmdExe.class);
    SpuController controller =
            new SpuController(
                    new SpuPageQueryExe(gateway),
                    new SpuDetailQueryExe(gateway),
                    create,
                    mock(SpuUpdateCmdExe.class),
                    mock(SpuShelfCmdExe.class));

    @Test
    void pageDefaultsAndTopLevelRowsFollowExistingContract() throws Exception {
        when(gateway.page(new CatalogFilter(null, null, 0, 10)))
                .thenReturn(new CatalogPage(List.of(), 0));
        MockMvcBuilders.standaloneSetup(controller)
                .build()
                .perform(get("/mall/spus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows").isEmpty())
                .andExpect(jsonPath("$.total").value(0));
        verify(gateway).page(new CatalogFilter(null, null, 0, 10));
    }

    @Test
    void rejectsInvalidPagingAndNestedSkuBeforeApplicationWrite() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(get("/mall/spus?pageSize=101")).andExpect(status().isBadRequest());
        mvc.perform(
                        post("/mall/spus")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"code\":\"SP1\",\"name\":\"goods\",\"categoryId\":1,\"brandId\":1,\"skus\":[]}"))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/mall/spus")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"code\":\"SP1\",\"name\":\"goods\",\"categoryId\":1,\"brandId\":1,\"skus\":[{\"code\":\"S1\",\"name\":\"sku\",\"price\":1.001,\"stock\":1}]}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(create);
    }

    @Test
    void acceptsNormalizedStringIdentifiersAndDecimalAmountsFromAdminUi() throws Exception {
        when(create.execute(any()))
                .thenReturn(
                        new com.lemnt.mall.application.catalog.dto.SpuDTO(
                                3L, "SP1", "goods", 1L, 2L, null, null, "OFF_SHELF", List.of()));
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(
                        post("/mall/spus")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
{"code":"SP1","name":"goods","categoryId":"1","brandId":"2",
 "skus":[{"code":"S1","name":"sku","price":"9.25","stock":5,"properties":{}}]}
"""))
                .andExpect(status().isOk());
        var captured =
                org.mockito.ArgumentCaptor.forClass(
                        com.lemnt.mall.application.catalog.cmd.SpuCreateCmd.class);
        verify(create).execute(captured.capture());
        assertEquals(1L, captured.getValue().categoryId());
        assertEquals(new java.math.BigDecimal("9.25"), captured.getValue().skus().get(0).price());
    }

    @Test
    void everyCatalogEndpointDeclaresAnExplicitPermission() {
        for (var type :
                List.of(
                        CategoryController.class,
                        BrandController.class,
                        SpuController.class,
                        SkuController.class)) {
            for (var method : type.getDeclaredMethods()) {
                if (!java.lang.reflect.Modifier.isPublic(method.getModifiers())) continue;
                var permissions = method.getAnnotation(RequiresPermissions.class);
                assertNotNull(permissions, type.getName() + "#" + method.getName());
                assertTrue(permissions.value()[0].startsWith("mall:"));
            }
        }
    }
}
