package com.lemnt.mall.adapter.web.catalog;

import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.adapter.web.catalog.SkuController;
import com.lemnt.mall.adapter.web.catalog.StockAdjustmentController;
import com.lemnt.mall.application.catalog.cmd.SpuCreateCmd;
import com.lemnt.mall.application.catalog.dto.SpuDTO;
import com.lemnt.mall.application.catalog.executor.SpuCreateCmdExe;
import com.lemnt.mall.application.catalog.executor.SpuDetailQueryExe;
import com.lemnt.mall.application.catalog.executor.SpuPageQueryExe;
import com.lemnt.mall.application.catalog.executor.SpuShelfCmdExe;
import com.lemnt.mall.application.catalog.executor.SpuUpdateCmdExe;
import com.lemnt.mall.application.catalog.service.CatalogService;
import com.lemnt.mall.domain.catalog.gateway.SpuGateway;
import com.lemnt.mall.domain.catalog.model.CatalogFilter;
import com.lemnt.mall.domain.catalog.model.CatalogPage;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CatalogHttpContractTest {
    CatalogService catalogService = mock(CatalogService.class);

    SpuGateway spuGateway = mock(SpuGateway.class);
    SpuCreateCmdExe create = mock(SpuCreateCmdExe.class);
    SpuController controller =
    new SpuController(new SpuPageQueryExe(spuGateway), new SpuDetailQueryExe(catalogService), create, mock(SpuUpdateCmdExe.class), mock(SpuShelfCmdExe.class));

    @Test
    void pageDefaultsAndTopLevelRowsFollowExistingContract() throws Exception {
        when(spuGateway.page(new CatalogFilter(null, null, 0, 10)))
            .thenReturn(new CatalogPage(List.of(), 0));
        MockMvcBuilders.standaloneSetup(controller)
            .build()
            .perform(get("/mall/spus"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rows").isEmpty())
            .andExpect(jsonPath("$.total").value(0));
        verify(spuGateway).page(new CatalogFilter(null, null, 0, 10));
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
                new SpuDTO(
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
        ArgumentCaptor.forClass(
                SpuCreateCmd.class);
        verify(create).execute(captured.capture());
        assertEquals(1L, captured.getValue().categoryId());
        assertEquals(new BigDecimal("9.25"), captured.getValue().skus().get(0).price());
    }

    @Test
    void everyCatalogEndpointDeclaresAnExplicitPermission() {
        for (var type :
                List.of(
                CategoryController.class,
                BrandController.class,
                SpuController.class,
                SkuController.class, StockAdjustmentController.class)) {
            for (var method : type.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    continue;
                }
                var permissions = method.getAnnotation(RequiresPermissions.class);
                assertNotNull(permissions, type.getName() + "#" + method.getName());
                assertTrue(permissions.value()[0].startsWith("mall:"));
            }
        }
    }
}
