package com.lemnt.mall.adapter.app.product;

import com.lemnt.mall.application.product.dto.StoreDetailDTO;
import com.lemnt.mall.application.product.dto.StorePageDTO;
import com.lemnt.mall.application.product.dto.StoreProductDTO;
import com.lemnt.mall.application.product.dto.StoreSkuDTO;
import com.lemnt.mall.application.product.executor.StoreCategoryListQueryExe;
import com.lemnt.mall.application.product.executor.StoreProductDetailQueryExe;
import com.lemnt.mall.application.product.executor.StoreProductPageQueryExe;
import com.lemnt.mall.application.product.qry.StorePageQry;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StoreHttpContractTest {
    StoreCategoryListQueryExe storeCategoryListQueryExe = mock(StoreCategoryListQueryExe.class);

    StoreProductPageQueryExe storeProductPageQueryExe = mock(StoreProductPageQueryExe.class);

    StoreProductDetailQueryExe storeProductDetailQueryExe = mock(StoreProductDetailQueryExe.class);

    @Test
    void boundedQueryDefaultsAndExactStringPrices() throws Exception {
        when(storeProductPageQueryExe.execute(new StorePageQry("goods", 2L, 1, 20)))
                .thenReturn(
                new StorePageDTO(
                List.of(
                new StoreProductDTO(
                1L, "goods", 2L, 3L, null, "9.99", "12.50", true)),
                1));

        var mvc = MockMvcBuilders.standaloneSetup(new Object[] {
                new AppCategoryController(storeCategoryListQueryExe),
                new AppProductController(storeProductPageQueryExe, storeProductDetailQueryExe)
                }).build();

        mvc.perform(get("/mall/app/products?name=goods&categoryId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].minPrice").value("9.99"))
                .andExpect(jsonPath("$.total").value(1));

        mvc.perform(get("/mall/app/products?pageSize=101")).andExpect(status().isBadRequest());

        verify(storeProductPageQueryExe).execute(new StorePageQry("goods", 2L, 1, 20));

        verifyNoMoreInteractions(storeCategoryListQueryExe, storeProductPageQueryExe, storeProductDetailQueryExe);

    }

    @Test
    void hiddenDetailDoesNotExposeAdminFields() throws Exception {
        when(storeProductDetailQueryExe.execute(1L))
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
                List.of(new StoreSkuDTO.Property(2L, "颜色", 3L, "红"))))));

        var mvc = MockMvcBuilders.standaloneSetup(new Object[] {
                new AppCategoryController(storeCategoryListQueryExe),
                new AppProductController(storeProductPageQueryExe, storeProductDetailQueryExe)
                }).build();

        mvc.perform(get("/mall/app/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skus[0].properties[0].valueName").value("红"))
                .andExpect(jsonPath("$.data.skus[0].properties[0].valueId").value(3))
                .andExpect(jsonPath("$.data.code").doesNotExist())
                .andExpect(jsonPath("$.data.skus[0].code").doesNotExist());

        mvc.perform(get("/mall/app/products/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

    }
}
