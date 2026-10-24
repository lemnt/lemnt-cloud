package com.lemnt.common.excel.handler;

import cn.hutool.core.map.MapUtil;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.handler.CellWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.lemnt.common.core.annotation.Excel;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.excel.converter.ExcelExportAnnotationConverter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 表格头样式处理器
 *
 * @author wangxx
 */
public class ExcelStyleHandler implements CellWriteHandler {

    private final Map<Integer, Excel> excelMap;
    private final Map<Integer, Object> lastRowValues = new HashMap<>();
    private final Map<Integer, BigDecimal> statisticsMap = new HashMap<>();
    private Sheet lastSheet;

    public ExcelStyleHandler(Class<?> clazz) {
        this.excelMap = ExcelExportAnnotationConverter.buildHeaderStyle(clazz);
    }

    @Override
    public void afterCellDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder,
                                 List<WriteCellData<?>> cellDataList, Cell cell, Head head,
                                 Integer relativeRowIndex, Boolean isHead) {
        if (Objects.isNull(cell)) {
            return;
        }
        Workbook workbook = writeSheetHolder.getSheet().getWorkbook();
        this.lastSheet = writeSheetHolder.getSheet();
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();

        Excel excel = excelMap.get(cell.getColumnIndex());
        if (Objects.isNull(excel)) {
            return;
        }
        if (Boolean.TRUE.equals(isHead)) {
            // === 表头样式 ===
            font.setBold(true);
            font.setFontHeightInPoints((short) 12);
            font.setColor(excel.headerColor().getIndex());
            style.setFont(font);
            style.setFillForegroundColor(excel.headerBackgroundColor().getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setAlignment(toHorizontalAlignment(excel.align()));
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            cell.setCellStyle(style);
            // 设置行高
            Row row = cell.getRow();
            if (row != null) {
                row.setHeightInPoints((float) excel.height());
            }
            // 提示信息
            if (StringUtils.isNotBlank(excel.prompt())) {
                addPrompt(cell, excel.prompt());
            }
        } else {
            font.setBold(false);
            font.setColor(excel.color().getIndex());
            style.setFont(font);
            style.setAlignment(toHorizontalAlignment(excel.align()));
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            // 可按列动态设置背景色
            style.setFillForegroundColor(excel.backgroundColor().getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            // 允许内容换行
            style.setWrapText(excel.wrapText());
            cell.setCellStyle(style);
            // 下拉
            if (excel.combo().length > 0) {
                addCombo(cell, excel.combo());
            }
            // 纵向合并
            if (excel.needMerge()) {
                mergeVertically(cell);
            }
            // 统计数据
            if (excel.isStatistics() && Objects.equals(excel.cellType(), Excel.ColumnType.NUMERIC)) {
                try {
                    BigDecimal number = BigDecimal.valueOf(cell.getNumericCellValue());
                    statisticsMap.merge(cell.getColumnIndex(), number, BigDecimal::add);
                } catch (Exception e) {
                    // 忽略无法转换的值
                }
            }
        }
    }

    /**
     * 给单元格添加下拉列表
     *
     * @param cell  当前单元格
     * @param combo 下拉可选值
     */
    private void addCombo(Cell cell, String[] combo) {
        DataValidationHelper helper = this.lastSheet.getDataValidationHelper();
        // 创建下拉约束
        DataValidationConstraint constraint = helper.createExplicitListConstraint(combo);
        // 单元格范围，这里只限制当前单元格
        CellRangeAddressList addressList = new CellRangeAddressList(
                cell.getRowIndex(), cell.getRowIndex(),
                cell.getColumnIndex(), cell.getColumnIndex()
        );

        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        this.lastSheet.addValidationData(validation);
    }

    /**
     * 给表头单元格添加提示信息
     *
     * @param cell   当前单元格
     * @param prompt 提示文本
     */
    private void addPrompt(Cell cell, String prompt) {
        DataValidationHelper helper = this.lastSheet.getDataValidationHelper();
        // 创建约束，这里用 TRUE 表示不限制输入
        DataValidationConstraint constraint = helper.createCustomConstraint("TRUE");
        // 只对当前单元格生效
        CellRangeAddressList addressList = new CellRangeAddressList(
                cell.getRowIndex(), cell.getRowIndex(),
                cell.getColumnIndex(), cell.getColumnIndex()
        );
        DataValidation validation = helper.createValidation(constraint, addressList);
        validation.createPromptBox("提示", prompt);
        validation.setShowPromptBox(true);
        this.lastSheet.addValidationData(validation);
    }

    /**
     * 纵向合并
     */
    private void mergeVertically(Cell cell) {
        int colIndex = cell.getColumnIndex();
        int rowIndex = cell.getRowIndex();
        Object currentValue = cell.getStringCellValue();

        Object lastValue = lastRowValues.get(colIndex);
        if (currentValue.equals(lastValue)) {
            // 扩展已有合并单元格
            CellRangeAddress merged = findMergedRegion(rowIndex - 1, colIndex);
            if (merged != null) {
                merged.setLastRow(rowIndex);
            } else {
                this.lastSheet.addMergedRegion(new CellRangeAddress(rowIndex - 1, rowIndex, colIndex, colIndex));
            }
        }
        lastRowValues.put(colIndex, currentValue);
    }

    /**
     * 查找已有合并单元格
     */
    private CellRangeAddress findMergedRegion(int row, int col) {
        for (int i = 0; i < this.lastSheet.getNumMergedRegions(); i++) {
            CellRangeAddress range = this.lastSheet.getMergedRegion(i);
            if (range.getFirstColumn() <= col && range.getLastColumn() >= col &&
                    range.getFirstRow() <= row && range.getLastRow() >= row) {
                return range;
            }
        }
        return null;
    }

    /**
     * 将 Excel 注解对齐方式转换为 POI 对齐方式
     *
     * @param align Excel 对齐枚举
     * @return POI 对齐枚举
     */
    private static HorizontalAlignment toHorizontalAlignment(Excel.ExcelAlign align) {
        if (align == null) {
            return HorizontalAlignment.CENTER;
        }
        return switch (align) {
            case LEFT -> HorizontalAlignment.LEFT;
            case RIGHT -> HorizontalAlignment.RIGHT;
            case GENERAL -> HorizontalAlignment.GENERAL;
            default -> HorizontalAlignment.CENTER;
        };
    }

    /**
     * 追加统计行
     */
    public void appendStatisticsRow() {
        if (MapUtil.isEmpty(statisticsMap)) {
            return;
        }
        int lastRowIndex = this.lastSheet.getLastRowNum() + 1;
        Row row = this.lastSheet.createRow(lastRowIndex);
        for (Map.Entry<Integer, BigDecimal> entry : statisticsMap.entrySet()) {
            int colIndex = entry.getKey();
            BigDecimal total = entry.getValue();
            Cell cell = row.createCell(colIndex);
            cell.setCellValue(total.doubleValue());
        }
        Cell firstCell = row.createCell(0);
        firstCell.setCellValue("总计");
    }
}
