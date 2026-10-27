package com.lemnt.mall.application.catalog;

import com.lemnt.mall.application.catalog.cmd.CategorySaveCmd;
import com.lemnt.mall.application.catalog.cmd.SpuShelfCmd;
import com.lemnt.mall.application.catalog.cmd.StockAdjustCmd;
import com.lemnt.mall.application.catalog.executor.CategorySaveCmdExe;
import com.lemnt.mall.application.catalog.executor.SpuPageQueryExe;
import com.lemnt.mall.application.catalog.executor.SpuShelfCmdExe;
import com.lemnt.mall.application.catalog.executor.StockAdjustCmdExe;
import com.lemnt.mall.application.catalog.qry.SpuPageQry;
import com.lemnt.mall.application.catalog.service.CatalogService;
import com.lemnt.mall.application.catalog.service.CatalogValidationService;
import com.lemnt.mall.domain.catalog.gateway.BrandGateway;
import com.lemnt.mall.domain.catalog.gateway.CategoryGateway;
import com.lemnt.mall.domain.catalog.gateway.SpuGateway;
import com.lemnt.mall.domain.catalog.model.Brand;
import com.lemnt.mall.domain.catalog.model.CatalogSpu;
import com.lemnt.mall.domain.catalog.model.Category;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogExecutorsTest {
    BrandGateway brandGateway = mock(BrandGateway.class);
    CatalogService catalogService = mock(CatalogService.class);
    CategoryGateway categoryGateway = mock(CategoryGateway.class);
    SpuGateway spuGateway = mock(SpuGateway.class);
    CatalogValidationService validation = new CatalogValidationService(brandGateway, categoryGateway);

    @Test
    void categoriesRejectThirdLevelAndReparenting() {
        var executor = new CategorySaveCmdExe(categoryGateway);
        when(categoryGateway.category(2L)).thenReturn(new Category(2L, 1L, "child", null, 0, "ENABLED"));
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
        when(categoryGateway.category(2L)).thenReturn(new Category(2L, 1L, "child", null, 0, "ENABLED"));
        when(categoryGateway.category(1L))
            .thenReturn(new Category(1L, null, "parent", null, 0, "DISABLED"));
        when(brandGateway.brand(3L)).thenReturn(new Brand(3L, "brand", null, null, 0, "ENABLED"));
        assertThrows(IllegalArgumentException.class, () -> validation.enabledReferences(2L, 3L));
    }

    @Test
    void pagingIsBoundedBeforeDatabaseAccess() {
        var executor = new SpuPageQueryExe(spuGateway);
        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute(new SpuPageQry(null, null, 0, 10)));
        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute(new SpuPageQry(null, null, 1, 101)));
        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute(new SpuPageQry(null, "unknown", 1, 10)));
        verifyNoInteractions(brandGateway, catalogService, categoryGateway, spuGateway);
    }

    @Test
    void offShelfDoesNotRequireEnabledTaxonomyButPublishDoes() {
        when(catalogService.spu(1L))
            .thenReturn(
                new CatalogSpu(
                1L, "S1", "name", 2L, 3L, null, null, "ON_SHELF", List.of()));
        when(spuGateway.shelf(1L, false)).thenReturn(true);
        var executor = new SpuShelfCmdExe(catalogService, spuGateway, validation);
        assertTrue(executor.execute(new SpuShelfCmd(1L, false)));
        assertThrows(
                IllegalArgumentException.class, () -> executor.execute(new SpuShelfCmd(1L, true)));
    }

    @Test
    void stockAdjustmentRejectsZeroAndEmptyReason() {
        var executor = new StockAdjustCmdExe(catalogService);
        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute(new StockAdjustCmd(1L, "R1", 0, "reason")));
        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute(new StockAdjustCmd(1L, "R1", 1, " ")));
        verifyNoInteractions(brandGateway, catalogService, categoryGateway, spuGateway);
    }
}
