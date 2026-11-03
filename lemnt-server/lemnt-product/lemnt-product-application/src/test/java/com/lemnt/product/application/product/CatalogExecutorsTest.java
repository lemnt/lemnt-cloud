package com.lemnt.product.application.product;

import com.lemnt.product.application.product.cmd.CategorySaveCmd;
import com.lemnt.product.application.product.cmd.ProductSpuShelfCmd;
import com.lemnt.product.application.product.executor.CategorySaveCmdExe;
import com.lemnt.product.application.product.executor.ProductSpuPageQueryExe;
import com.lemnt.product.application.product.executor.ProductSpuShelfCmdExe;
import com.lemnt.product.application.product.qry.ProductSpuPageQry;
import com.lemnt.product.application.product.service.ProductCatalogService;
import com.lemnt.product.application.product.service.ProductValidationService;
import com.lemnt.product.domain.product.gateway.BrandGateway;
import com.lemnt.product.domain.product.gateway.CategoryGateway;
import com.lemnt.product.domain.product.gateway.ProductSpuGateway;
import com.lemnt.product.domain.product.model.Brand;
import com.lemnt.product.domain.product.model.Category;
import com.lemnt.product.domain.product.model.ProductSpu;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogExecutorsTest {
    BrandGateway brandGateway = mock(BrandGateway.class);
    ProductCatalogService catalogService = mock(ProductCatalogService.class);
    CategoryGateway categoryGateway = mock(CategoryGateway.class);
    ProductSpuGateway productSpuGateway = mock(ProductSpuGateway.class);
    ProductValidationService validation =
            new ProductValidationService(brandGateway, categoryGateway);

    @Test
    void categoriesRejectThirdLevelAndReparenting() {
        var executor = new CategorySaveCmdExe(categoryGateway);
        when(categoryGateway.category(2L))
                .thenReturn(new Category(2L, 1L, "child", null, 0, "ENABLED"));
        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute(new CategorySaveCmd(null, 2L, "bad", null, 0, "ENABLED")));
        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute(new CategorySaveCmd(2L, null, "bad", null, 0, "ENABLED")));
        verify(categoryGateway, never()).saveCategory(any());
    }

    @Test
    void disabledParentPreventsPublishing() {
        when(categoryGateway.category(2L))
                .thenReturn(new Category(2L, 1L, "child", null, 0, "ENABLED"));
        when(categoryGateway.category(1L))
                .thenReturn(new Category(1L, null, "parent", null, 0, "DISABLED"));
        when(brandGateway.brand(3L)).thenReturn(new Brand(3L, "brand", null, null, 0, "ENABLED"));
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
