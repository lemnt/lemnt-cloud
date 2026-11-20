package com.lemnt.product.application.product;

import com.lemnt.product.application.product.cmd.ProductBrowseHistoryCreateCmd;
import com.lemnt.product.application.product.executor.ProductCategoryEnableListQueryExe;
import com.lemnt.product.application.product.executor.StoreProductDetailQueryExe;
import com.lemnt.product.application.product.executor.StoreProductPageQueryExe;
import com.lemnt.product.application.product.qry.StorePageQry;
import com.lemnt.product.application.product.service.ProductBrowseHistoryService;
import com.lemnt.product.application.product.service.ProductCatalogService;
import com.lemnt.product.domain.product.gateway.ProductCategoryGateway;
import com.lemnt.product.domain.product.gateway.ProductSpuGateway;
import com.lemnt.product.domain.product.model.ProductCategory;
import com.lemnt.product.domain.product.model.ProductSku;
import com.lemnt.product.domain.product.model.ProductSkuSpec;
import com.lemnt.product.domain.product.model.ProductSpu;
import com.lemnt.product.domain.product.model.StoreFilter;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 验证分类只展开启用直接子类，排序和非法输入在数据库访问之前确定。 */
class StoreQueryTest {
    private final ProductSpuGateway products = mock(ProductSpuGateway.class);
    private final ProductCategoryGateway categories = mock(ProductCategoryGateway.class);
    private final StoreProductPageQueryExe query =
            new StoreProductPageQueryExe(products, categories);

    @Test
    void unionsInputAndEnabledDirectChildrenOnceAndPreservesDescending() {
        when(categories.enabledChildren(List.of(1L, 2L)))
                .thenReturn(List.of(new ProductCategory(3L, 1L, "child", null, 0, "ENABLED")));
        query.execute(new StorePageQry("goods", 1L, 2, 20, List.of(2L, 1L), "price", false));
        var expected = new StoreFilter("goods", null, 20, 20, List.of(1L, 2L, 3L), "price", false);
        verify(products).products(expected);
        verify(products).storeCount(expected);
        verify(categories).enabledChildren(List.of(1L, 2L));
        verifyNoMoreInteractions(categories);
    }

    @Test
    void noCategoryDoesNotRestrictCatalogAndDirectionDefaultsAscending() {
        query.execute(new StorePageQry(null, null, 1, 20, List.of(), "salesCount", null));
        verify(products)
                .products(new StoreFilter(null, null, 0, 20, List.of(), "salesCount", true));
        verifyNoInteractions(categories);
    }

    @Test
    void rejectsUnknownSortAndInvalidIdsBeforeGatewayCalls() {
        assertThrows(
                IllegalArgumentException.class,
                () -> query.execute(new StorePageQry(null, null, 1, 20, null, "updateTime", null)));
        assertThrows(
                IllegalArgumentException.class,
                () -> query.execute(new StorePageQry(null, null, 1, 20, List.of(0L), null, null)));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProductCategoryEnableListQueryExe(categories).execute(List.of(-1L)));
        verifyNoInteractions(products, categories);
    }

    @Test
    void projectsStoredDetailsAndPreservesUnknownFields() {
        var catalog = mock(ProductCatalogService.class);
        var sku =
                new ProductSku(
                        7L,
                        1L,
                        "sku",
                        "red",
                        new BigDecimal("10.00"),
                        2,
                        null,
                        List.<ProductSkuSpec>of(),
                        "ON_SHELF",
                        new BigDecimal("1.250"),
                        null);
        when(catalog.product(1L))
                .thenReturn(
                        new ProductSpu(
                                1L,
                                "spu",
                                "goods",
                                2L,
                                3L,
                                "description",
                                null,
                                "ON_SHELF",
                                List.of(sku),
                                List.of(1, 2),
                                8L,
                                null,
                                "intro",
                                List.of("https://example.test/a.png"),
                                1));
        var browse = mock(ProductBrowseHistoryService.class);
        when(products.recordBrowse(1L)).thenReturn(true);
        var executor = new StoreProductDetailQueryExe(catalog, products, browse);
        var result = executor.execute(1L, 5L);
        assertEquals("intro", result.introduction());
        assertEquals(List.of("https://example.test/a.png"), result.sliderPicUrls());
        assertEquals(List.of(1, 2), result.deliveryTypes());
        assertEquals(8L, result.deliveryTemplateId());
        assertEquals(new BigDecimal("1.250"), result.skus().get(0).weight());
        assertNull(result.skus().get(0).volume());
        verify(browse).create(new ProductBrowseHistoryCreateCmd(5L, 1L));
        assertNull(executor.execute(99L, null));
        verify(products, never()).recordBrowse(99L);
    }
}
