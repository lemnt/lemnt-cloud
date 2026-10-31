package com.lemnt.mall.application.product;

import com.lemnt.mall.application.product.dto.ProductSpuDTO;
import com.lemnt.mall.application.product.dto.ProductSpuPageDTO;
import com.lemnt.mall.application.product.executor.ProductSpuExportQueryExe;
import com.lemnt.mall.application.product.executor.ProductSpuPageQueryExe;
import com.lemnt.mall.application.product.qry.ProductSpuExportQry;
import com.lemnt.mall.application.product.qry.ProductSpuPageQry;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 导出覆盖完整分页、不静默截断，并拒绝超出同步导出范围的数据。 */
class ProductSpuExportQueryTest {
    private final ProductSpuDTO row = new ProductSpuDTO(1L, "SP1", "商品", 2L, 3L,
            null, null, "OFF_SHELF", List.of());

    @Test
    void readsEveryPageUsingSameBusinessAndStockFilters() {
        var query = mock(ProductSpuPageQueryExe.class);
        when(query.execute(any())).thenReturn(new ProductSpuPageDTO(Collections.nCopies(100, row), 101))
                .thenReturn(new ProductSpuPageDTO(List.of(row), 101));
        assertEquals(101, new ProductSpuExportQueryExe(query).execute(
                new ProductSpuExportQry("商品", null, 2L, 3L, "LOW_STOCK")).size());
        verify(query).execute(new ProductSpuPageQry("商品", null, 1, 100, 2L, 3L, "LOW_STOCK"));
        verify(query).execute(new ProductSpuPageQry("商品", null, 2, 100, 2L, 3L, "LOW_STOCK"));
        verifyNoMoreInteractions(query);
    }

    @Test
    void rejectsOversizedAndIncompleteResultsInsteadOfExportingFirstPage() {
        var query = mock(ProductSpuPageQueryExe.class);
        var exporter = new ProductSpuExportQueryExe(query);
        when(query.execute(any())).thenReturn(new ProductSpuPageDTO(List.of(row), 10001));
        assertThrows(IllegalArgumentException.class, () -> exporter.execute(
                new ProductSpuExportQry(null, null, null, null, null)));
        reset(query);
        when(query.execute(any())).thenReturn(new ProductSpuPageDTO(List.of(), 1));
        assertThrows(IllegalStateException.class, () -> exporter.execute(
                new ProductSpuExportQry(null, null, null, null, null)));
    }
}
