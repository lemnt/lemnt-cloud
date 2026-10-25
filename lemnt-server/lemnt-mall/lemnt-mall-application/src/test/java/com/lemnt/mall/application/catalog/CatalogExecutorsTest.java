package com.lemnt.mall.application.catalog;

import com.lemnt.mall.application.catalog.cmd.*;
import com.lemnt.mall.application.catalog.executor.*;
import com.lemnt.mall.application.catalog.qry.SpuPageQry;
import com.lemnt.mall.application.catalog.service.CatalogValidationService;
import com.lemnt.mall.domain.catalog.gateway.CatalogGateway;
import com.lemnt.mall.domain.catalog.model.*;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CatalogExecutorsTest {
    CatalogGateway gateway = mock(CatalogGateway.class);
    CatalogValidationService validation = new CatalogValidationService(gateway);

    @Test
    void categoriesRejectThirdLevelAndReparenting() {
        var executor = new CategorySaveCmdExe(gateway);
        when(gateway.category(2L)).thenReturn(new Category(2L, 1L, "child", null, 0, "ENABLED"));
        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute(new CategorySaveCmd(null, 2L, "bad", null, 0, "ENABLED")));
        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute(new CategorySaveCmd(2L, null, "bad", null, 0, "ENABLED")));
        verify(gateway, never()).saveCategory(any());
    }

    @Test
    void disabledParentPreventsPublishing() {
        when(gateway.category(2L)).thenReturn(new Category(2L, 1L, "child", null, 0, "ENABLED"));
        when(gateway.category(1L))
                .thenReturn(new Category(1L, null, "parent", null, 0, "DISABLED"));
        when(gateway.brand(3L)).thenReturn(new Brand(3L, "brand", null, null, 0, "ENABLED"));
        assertThrows(IllegalArgumentException.class, () -> validation.enabledReferences(2L, 3L));
    }

    @Test
    void pagingIsBoundedBeforeDatabaseAccess() {
        var executor = new SpuPageQueryExe(gateway);
        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute(new SpuPageQry(null, null, 0, 10)));
        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute(new SpuPageQry(null, null, 1, 101)));
        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute(new SpuPageQry(null, "unknown", 1, 10)));
        verifyNoInteractions(gateway);
    }

    @Test
    void offShelfDoesNotRequireEnabledTaxonomyButPublishDoes() {
        when(gateway.spu(1L))
                .thenReturn(
                        new CatalogSpu(
                                1L, "S1", "name", 2L, 3L, null, null, "ON_SHELF", List.of()));
        when(gateway.shelf(1L, false)).thenReturn(true);
        var executor = new SpuShelfCmdExe(gateway, validation);
        assertTrue(executor.execute(new SpuShelfCmd(1L, false)));
        assertThrows(
                IllegalArgumentException.class, () -> executor.execute(new SpuShelfCmd(1L, true)));
    }

    @Test
    void stockAdjustmentRejectsZeroAndEmptyReason() {
        var executor = new StockAdjustCmdExe(gateway);
        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute(new StockAdjustCmd(1L, "R1", 0, "reason")));
        assertThrows(
                IllegalArgumentException.class,
                () -> executor.execute(new StockAdjustCmd(1L, "R1", 1, " ")));
        verifyNoInteractions(gateway);
    }
}
