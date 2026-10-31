package com.lemnt.mall.adapter.web.product;

import com.lemnt.mall.application.product.dto.ProductSpuDTO;
import com.lemnt.mall.application.product.executor.ProductSpuExportQueryExe;
import com.lemnt.mall.application.product.qry.ProductSpuExportQry;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 检查实际XLSX文件而非仅检查返回头，包含大编号和公式样式文本。 */
class ProductSpuExportHttpTest {
    @Test
    void returnsWorkbookWithTextIdsLiteralNamesAndCompleteFilterBinding() throws Exception {
        var export = mock(ProductSpuExportQueryExe.class);
        when(export.execute(any())).thenReturn(List.of(new ProductSpuDTO(Long.MAX_VALUE, "SP1", "=1+1",
                2L, 3L, "真实描述", null, "OFF_SHELF", List.of(), List.of(), null, "关键字", "简介", List.of("/a.png", "/b.png"), 12)));
        var mvc = MockMvcBuilders.standaloneSetup(new ProductSpuExportController(export)).build();
        var response = mvc.perform(get("/mall/spus/export").param("name", "商品")
                        .param("categoryId", "2").param("brandId", "3").param("stockStatus", "LOW_STOCK")
                        .param("pageNum", "99").param("pageSize", "1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=products.xlsx"))
                .andReturn().getResponse().getContentAsByteArray();
        verify(export).execute(new ProductSpuExportQry("商品", null, 2L, 3L, "LOW_STOCK"));
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(response))) {
            var sheet = workbook.getSheetAt(0);
            assertEquals("商品编号", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals(Long.toString(Long.MAX_VALUE), sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals(CellType.STRING, sheet.getRow(1).getCell(0).getCellType());
            assertEquals("=1+1", sheet.getRow(1).getCell(2).getStringCellValue());
            assertEquals(CellType.STRING, sheet.getRow(1).getCell(2).getCellType());
            assertEquals("下架", sheet.getRow(1).getCell(5).getStringCellValue());
            assertEquals("关键字", sheet.getRow(1).getCell(7).getStringCellValue());
            assertEquals("简介", sheet.getRow(1).getCell(8).getStringCellValue());
            assertEquals("/a.png\n/b.png", sheet.getRow(1).getCell(9).getStringCellValue());
            assertEquals(12, sheet.getRow(1).getCell(10).getNumericCellValue());
        }
        mvc.perform(get("/mall/spus/export").param("categoryId", "0"))
                .andExpect(status().isBadRequest());
        verifyNoMoreInteractions(export);
    }
}
