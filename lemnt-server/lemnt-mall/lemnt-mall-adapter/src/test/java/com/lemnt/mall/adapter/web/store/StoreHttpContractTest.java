package com.lemnt.mall.adapter.web.store;

import com.lemnt.mall.application.store.executor.StoreCatalogQueryExe;
import com.lemnt.mall.application.store.qry.StorePageQry;
import com.lemnt.mall.application.store.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class StoreHttpContractTest {
    @Test
    void boundedQueryDefaultsAndExactStringPrices() throws Exception {
        var q = mock(StoreCatalogQueryExe.class);
        when(q.execute(new StorePageQry("goods", 2L, 1, 20)))
                .thenReturn(
                        new StorePageDTO(
                                List.of(
                                        new StoreProductDTO(
                                                1L, "goods", 2L, 3L, null, "9.99", "12.50", true)),
                                1));
        var mvc = MockMvcBuilders.standaloneSetup(new AppStoreController(q)).build();
        mvc.perform(get("/mall/app/products?name=goods&categoryId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].minPrice").value("9.99"))
                .andExpect(jsonPath("$.data.total").value(1));
        mvc.perform(get("/mall/app/products?pageSize=101")).andExpect(status().isBadRequest());
        verify(q).execute(new StorePageQry("goods", 2L, 1, 20));
        verifyNoMoreInteractions(q);
    }

    @Test
    void hiddenDetailDoesNotExposeAdminFields() throws Exception {
        var q = mock(StoreCatalogQueryExe.class);
        when(q.execute(1L))
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
                                                java.util.Map.of("颜色", "红")))));
        var mvc = MockMvcBuilders.standaloneSetup(new AppStoreController(q)).build();
        mvc.perform(get("/mall/app/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skus[0].properties.颜色").value("红"))
                .andExpect(jsonPath("$.data.code").doesNotExist())
                .andExpect(jsonPath("$.data.skus[0].code").doesNotExist());
        mvc.perform(get("/mall/app/products/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
