package com.lemnt.product.adapter.app.product;

import com.lemnt.product.application.product.dto.StoreDetailDTO;
import com.lemnt.product.application.product.dto.StorePageDTO;
import com.lemnt.product.application.product.dto.StoreProductDTO;
import com.lemnt.product.application.product.dto.StoreSkuDTO;
import com.lemnt.product.application.product.executor.ProductCategoryEnableListQueryExe;
import com.lemnt.product.application.product.executor.StoreProductDetailQueryExe;
import com.lemnt.product.application.product.executor.StoreProductPageQueryExe;
import com.lemnt.product.application.product.qry.StorePageQry;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StoreHttpContractTest {
    ProductCategoryEnableListQueryExe storeCategoryListQueryExe =
            mock(ProductCategoryEnableListQueryExe.class);

    StoreProductPageQueryExe storeProductPageQueryExe = mock(StoreProductPageQueryExe.class);

    StoreProductDetailQueryExe storeProductDetailQueryExe = mock(StoreProductDetailQueryExe.class);

    @Test
    void boundedQueryDefaultsAndExactStringPrices() throws Exception {
        when(storeProductPageQueryExe.execute(new StorePageQry("goods", 2L, 1, 20)))
                .thenReturn(
                        new StorePageDTO(
                                List.of(
                                        new StoreProductDTO(
                                                1L, "goods", 2L, 3L, null, "9.99", "12.50", true,
                                                7)),
                                1));

        var mvc =
                MockMvcBuilders.standaloneSetup(
                                new Object[] {
                                    new AppProductCategoryController(storeCategoryListQueryExe),
                                    new AppProductController(
                                            storeProductPageQueryExe, storeProductDetailQueryExe)
                                })
                        .build();

        mvc.perform(get("/app/products?name=goods&categoryId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].minPrice").value("9.99"))
                .andExpect(jsonPath("$.rows[0].salesCount").value(7))
                .andExpect(jsonPath("$.total").value(1));

        mvc.perform(get("/app/products?pageSize=101")).andExpect(status().isBadRequest());

        verify(storeProductPageQueryExe).execute(new StorePageQry("goods", 2L, 1, 20));

        verifyNoMoreInteractions(
                storeCategoryListQueryExe, storeProductPageQueryExe, storeProductDetailQueryExe);
    }

    @Test
    void hiddenDetailDoesNotExposeAdminFields() throws Exception {
        when(storeProductDetailQueryExe.execute(1L, null))
                .thenReturn(
                        new StoreDetailDTO(
                                1L,
                                "goods",
                                2L,
                                3L,
                                "plain",
                                null,
                                List.of(
                                        new StoreSkuDTO(
                                                7L,
                                                "red",
                                                "10.00",
                                                3,
                                                null,
                                                List.of(new StoreSkuDTO.Spec(2L, "颜色", 3L, "红")))),
                                7));

        var mvc =
                MockMvcBuilders.standaloneSetup(
                                new Object[] {
                                    new AppProductCategoryController(storeCategoryListQueryExe),
                                    new AppProductController(
                                            storeProductPageQueryExe, storeProductDetailQueryExe)
                                })
                        .build();

        mvc.perform(get("/app/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skus[0].specs[0].specValueName").value("红"))
                .andExpect(jsonPath("$.data.skus[0].specs[0].specValueId").value(3))
                .andExpect(jsonPath("$.data.salesCount").value(7))
                .andExpect(jsonPath("$.data.code").doesNotExist())
                .andExpect(jsonPath("$.data.skus[0].code").doesNotExist());

        mvc.perform(get("/app/products/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void bindsMultiCategoryAndFalseSortAndRejectsUnknownField() throws Exception {
        var expected = new StorePageQry(null, 1L, 1, 20, List.of(2L, 3L), "price", false);
        when(storeProductPageQueryExe.execute(expected)).thenReturn(new StorePageDTO(List.of(), 0));
        var mvc =
                MockMvcBuilders.standaloneSetup(
                                new AppProductController(
                                        storeProductPageQueryExe, storeProductDetailQueryExe))
                        .build();
        mvc.perform(
                        get("/app/products")
                                .param("categoryId", "1")
                                .param("categoryIds", "2,3")
                                .param("sortField", "price")
                                .param("sortAsc", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
        verify(storeProductPageQueryExe).execute(expected);
        mvc.perform(get("/app/products").param("sortField", "price desc"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/app/products").param("categoryIds", "0"))
                .andExpect(status().isBadRequest());
        verifyNoMoreInteractions(storeProductPageQueryExe);
    }

    @Test
    void categoryIdsUseExistingPublicEndpointAndKeepResponseShape() throws Exception {
        when(storeCategoryListQueryExe.execute(List.of(2L, 3L))).thenReturn(List.of());
        var mvc =
                MockMvcBuilders.standaloneSetup(
                                new AppProductCategoryController(storeCategoryListQueryExe))
                        .build();
        mvc.perform(get("/app/categories").param("ids", "2,3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
        verify(storeCategoryListQueryExe).execute(List.of(2L, 3L));
    }

    @Test
    void existingDisplayAndDeliveryFieldsReachHttpWithoutFillingUnknowns() throws Exception {
        when(storeProductDetailQueryExe.execute(1L, null))
                .thenReturn(
                        new StoreDetailDTO(
                                1L,
                                "goods",
                                2L,
                                3L,
                                "plain",
                                null,
                                List.of(
                                        new StoreSkuDTO(
                                                7L,
                                                "sku",
                                                "10.00",
                                                2,
                                                null,
                                                List.of(),
                                                new BigDecimal("1.250"),
                                                null)),
                                7,
                                "intro",
                                List.of("https://example.test/p.png"),
                                List.of(1, 2),
                                8L));
        var mvc =
                MockMvcBuilders.standaloneSetup(
                                new AppProductController(
                                        storeProductPageQueryExe, storeProductDetailQueryExe))
                        .build();
        mvc.perform(get("/app/products/1"))
                .andExpect(jsonPath("$.data.introduction").value("intro"))
                .andExpect(jsonPath("$.data.sliderPicUrls[0]").value("https://example.test/p.png"))
                .andExpect(jsonPath("$.data.deliveryTypes[1]").value(2))
                .andExpect(jsonPath("$.data.deliveryTemplateId").value(8))
                .andExpect(jsonPath("$.data.skus[0].weight").value(1.25))
                .andExpect(jsonPath("$.data.skus[0].volume").isEmpty());
    }
}
