package com.lemnt.common.excel.handler;

import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.style.column.AbstractColumnWidthStyleStrategy;
import com.lemnt.common.excel.converter.ExcelExportAnnotationConverter;
import org.apache.poi.ss.usermodel.Cell;

import java.util.List;
import java.util.Map;

/**
 * 表格列宽 Handler
 *
 * @author wangxx
 */
public class ExcelColumnWidthHandler extends AbstractColumnWidthStyleStrategy {

    private final Map<Integer, Double> widthMap;

    public ExcelColumnWidthHandler(Class<?> clazz) {
        this.widthMap = ExcelExportAnnotationConverter.buildColumnWidth(clazz);
    }

    @Override
    protected void setColumnWidth(WriteSheetHolder writeSheetHolder, List<WriteCellData<?>> cellDataList,
                                  Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
        Double width = widthMap.get(cell.getColumnIndex());
        if (width != null) {
            // EasyExcel 列宽 = 字符数 * 256
            writeSheetHolder.getSheet().setColumnWidth(cell.getColumnIndex(), (int) (width * 256));
        }
    }

}
