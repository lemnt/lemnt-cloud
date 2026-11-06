package com.lemnt.product.application.product;

import com.lemnt.product.application.product.cmd.ProductCategorySaveCmd;
import com.lemnt.product.application.product.cmd.ProductSpuShelfCmd;
import com.lemnt.product.application.product.executor.ProductCategorySaveCmdExe;
import com.lemnt.product.application.product.executor.ProductSpuPageQueryExe;
import com.lemnt.product.application.product.executor.ProductSpuShelfCmdExe;
import com.lemnt.product.application.product.qry.ProductSpuPageQry;
import com.lemnt.product.application.product.service.ProductCatalogService;
import com.lemnt.product.application.product.service.ProductValidationService;
import com.lemnt.product.domain.product.gateway.ProductBrandGateway;
import com.lemnt.product.domain.product.gateway.ProductCategoryGateway;
import com.lemnt.product.domain.product.gateway.ProductSpuGateway;
import com.lemnt.product.domain.product.model.ProductBrand;
import com.lemnt.product.domain.product.model.ProductCategory;
import com.lemnt.product.domain.product.model.ProductSpu;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogExecutorsTest {
    ProductBrandGateway brandGateway = mock(ProductBrandGateway.class);
    ProductCatalogService catalogService = mock(ProductCatalogService.class);
    ProductCategoryGateway categoryGateway = mock(ProductCategoryGateway.class);
    ProductSpuGateway productSpuGateway = mock(ProductSpuGateway.class);
    ProductValidationService validation =
        new ProductValidationService(brandGateway, categoryGateway);

    @Test
    void categoriesRejectThirdLevelAndReparenting() {
        var executor = new ProductCategorySaveCmdExe(categoryGateway);
        when(categoryGateway.category(2L))
            .thenReturn(new ProductCategory(2L, 1L, "child", null, 0, "ENABLED"));
        assertThrows(
            IllegalArgumentException.class,
            () -> executor.execute(new ProductCategorySaveCmd(null, 2L, "bad", null, 0, "ENABLED")));
        assertThrows(
            IllegalArgumentException.class,
            () -> executor.execute(new ProductCategorySaveCmd(2L, null, "bad", null, 0, "ENABLED")));
        verify(categoryGateway, never()).saveCategory(any());
    }

    @Test
    void disabledParentPreventsPublishing() {
        when(categoryGateway.category(2L))
            .thenReturn(new ProductCategory(2L, 1L, "child", null, 0, "ENABLED"));
        when(categoryGateway.category(1L))
            .thenReturn(new ProductCategory(1L, null, "parent", null, 0, "DISABLED"));
        when(brandGateway.brand(3L)).thenReturn(new ProductBrand(3L, "brand", null, null, 0, "ENABLED"));
        assertThrows(IllegalArgumentException.class, () -> validation.enabledReferences(2L, 3L));
    }

    @Test
    void pagingIsBoundedBeforeDatabaseAccess() {
        var executor = new ProductSpuPageQueryExe(productSpuGateway);
        assertThrows(
            IllegalArgumentException.class,
            () -> executor.execute(new ProductSpuPageQry(null, null, 0, 10, null, null, null)));
        assertThrows(
            IllegalArgumentException.class,
            () ->
                executor.execute(
                    new ProductSpuPageQry(null, null, 1, 101, null, null, null)));
        assertThrows(
            IllegalArgumentException.class,
            () ->
                executor.execute(
                    new ProductSpuPageQry(null, "unknown", 1, 10, null, null, null)));
        verifyNoInteractions(brandGateway, catalogService, categoryGateway, productSpuGateway);
    }

    @Test
    void offShelfDoesNotRequireEnabledTaxonomyButPublishDoes() {
        when(catalogService.spu(1L))
            .thenReturn(
                new ProductSpu(
                    1L, "S1", "name", 2L, 3L, null, null, "ON_SHELF", List.of()));
        when(productSpuGateway.shelf(1L, false)).thenReturn(true);
        var executor = new ProductSpuShelfCmdExe(catalogService, productSpuGateway, validation);
        assertTrue(executor.execute(new ProductSpuShelfCmd(1L, false)));
        assertThrows(
            IllegalArgumentException.class,
            () -> executor.execute(new ProductSpuShelfCmd(1L, true)));
    }
}
