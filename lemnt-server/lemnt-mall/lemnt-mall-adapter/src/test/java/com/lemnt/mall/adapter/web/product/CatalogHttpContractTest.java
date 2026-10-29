package com.lemnt.mall.adapter.web.product;

import com.lemnt.common.security.annotation.RequiresPermissions;
import com.lemnt.mall.application.product.cmd.ProductSpuCreateCmd;
import com.lemnt.mall.application.product.dto.ProductSpuDTO;
import com.lemnt.mall.application.product.executor.ProductSpuCreateCmdExe;
import com.lemnt.mall.application.product.executor.ProductSpuDetailQueryExe;
import com.lemnt.mall.application.product.executor.ProductSpuPageQueryExe;
import com.lemnt.mall.application.product.executor.ProductSpuShelfCmdExe;
import com.lemnt.mall.application.product.executor.ProductSpuUpdateCmdExe;
import com.lemnt.mall.application.product.service.ProductCatalogService;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogFilter;
import com.lemnt.mall.domain.product.model.ProductCatalogPage;

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
    ProductCatalogService catalogService = mock(ProductCatalogService.class);

    ProductSpuGateway productSpuGateway = mock(ProductSpuGateway.class);
    ProductSpuCreateCmdExe create = mock(ProductSpuCreateCmdExe.class);
    ProductSpuController controller =
    new ProductSpuController(new ProductSpuPageQueryExe(productSpuGateway), new ProductSpuDetailQueryExe(catalogService), create, mock(ProductSpuUpdateCmdExe.class), mock(ProductSpuShelfCmdExe.class));

    @Test
    void pageDefaultsAndTopLevelRowsFollowExistingContract() throws Exception {
        when(productSpuGateway.page(new ProductCatalogFilter(null, null, 0, 10, null, null)))
            .thenReturn(new ProductCatalogPage(List.of(), 0));
        MockMvcBuilders.standaloneSetup(controller)
            .build()
            .perform(get("/mall/spus"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rows").isEmpty())
            .andExpect(jsonPath("$.total").value(0));
        verify(productSpuGateway).page(new ProductCatalogFilter(null, null, 0, 10, null, null));
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
                new ProductSpuDTO(
                3L, "SP1", "goods", 1L, 2L, null, null, "OFF_SHELF", List.of()));
        var mvc = MockMvcBuilders.standaloneSetup(controller).build();
        mvc.perform(
                post("/mall/spus")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                """
{"code":"SP1","name":"goods","categoryId":"1","brandId":"2","deliveryTypes":[],
 "skus":[{"code":"S1","name":"sku","price":"9.25","stock":5,"properties":[]}]}
"""))
            .andExpect(status().isOk());
        var captured =
        ArgumentCaptor.forClass(
                ProductSpuCreateCmd.class);
        verify(create).execute(captured.capture());
        assertEquals(1L, captured.getValue().categoryId());
        assertEquals(new BigDecimal("9.25"), captured.getValue().skus().get(0).price());
        mvc.perform(post("/mall/spus").contentType(MediaType.APPLICATION_JSON).content("""
                {"code":"SP1","name":"goods","categoryId":1,"brandId":2,"deliveryTypes":[null],
                 "skus":[{"code":"S1","name":"sku","price":"9.25","stock":5,"properties":[]}]}
                """)).andExpect(status().isBadRequest());
        verifyNoMoreInteractions(create);
    }

    @Test
    void everyCatalogEndpointDeclaresAnExplicitPermission() {
        for (var type :
                List.of(
                CategoryController.class,
                BrandController.class,
                ProductSpuController.class)) {
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
