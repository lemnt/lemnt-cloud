package com.lemnt.common.excel.converter;

import cn.hutool.core.util.StrUtil;
import com.lemnt.common.core.annotation.Excel;
import com.lemnt.common.core.annotation.Excels;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 表格导出注解转换器
 *
 * @author wangxx
 */
public class ExcelExportAnnotationConverter {

    /**
     * 构建表头（等价 @ExcelProperty）
     */
    public static List<List<String>> buildHead(Class<?> clazz) {
        List<List<String>> head = new ArrayList<>();
        for (Field f : getAllExportFields(clazz)) {
            if (f.isAnnotationPresent(Excels.class)) {
                Excels excels = f.getAnnotation(Excels.class);
                for (Excel e : excels.value()) {
                    head.add(List.of(e.name()));
                }
            } else if (f.isAnnotationPresent(Excel.class)) {
                head.add(List.of(f.getAnnotation(Excel.class).name()));
            }
        }
        return head;
    }

    /**
     * 转换数据行
     */
    public static <T> List<List<Object>> convertRows(List<T> data, Class<T> clazz) {
        List<List<Object>> rows = new ArrayList<>();
        List<Field> exportFields = getAllExportFields(clazz);
        for (T obj : data) {
            List<Object> row = new ArrayList<>();
            for (Field f : exportFields) {
                Object value = getValue(f, obj);
                if (value instanceof List) {
                    row.addAll((List<?>) value);
                } else {
                    row.add(value);
                }
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * 获取参与导出的字段（排序 + 过滤）
     */
    private static List<Field> getAllExportFields(Class<?> clazz) {
        // 支持继承字段
        List<Field> fieldList = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            fieldList.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }
        return fieldList.stream()
                .filter(f -> f.isAnnotationPresent(Excel.class) || f.isAnnotationPresent(Excels.class))
                .filter(ExcelExportAnnotationConverter::isExportField)
                .sorted(Comparator.comparingInt(ExcelExportAnnotationConverter::getSort))
                .peek(f -> f.setAccessible(true))
                .collect(Collectors.toList());
    }

    /**
     * 根据注解处理单元格值
     */
    private static Object getValue(Field field, Object obj) {
        try {
            field.setAccessible(true);
            if (field.isAnnotationPresent(Excels.class)) {
                Object nestedObj = field.get(obj);
                Excels excels = field.getAnnotation(Excels.class);
                List<Object> values = new ArrayList<>();
                for (Excel e : excels.value()) {
                    if (nestedObj != null) {
                        Field nestedField = nestedObj.getClass().getDeclaredField(e.targetAttr());
                        nestedField.setAccessible(true);
                        Object nestedValue = nestedField.get(nestedObj);
                        values.add(processExcelValue(nestedValue, e));
                    } else {
                        values.add(null);
                    }
                }
                return values;
            }
            if (field.isAnnotationPresent(Excel.class)) {
                Excel excel = field.getAnnotation(Excel.class);
                Object value = field.get(obj);
                return processExcelValue(value, excel);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static Object processExcelValue(Object value, Excel excel) {
        if (value == null && StrUtil.isNotBlank(excel.defaultValue())) {
            return excel.defaultValue();
        }
        try {
            if (value instanceof Date && StrUtil.isNotBlank(excel.dateFormat())) {
                return new SimpleDateFormat(excel.dateFormat()).format(value);
            }
            if (value instanceof BigDecimal && excel.scale() >= 0) {
                BigDecimal bd = ((BigDecimal) value).setScale(excel.scale(), excel.roundingMode());
                return StrUtil.isEmpty(excel.suffix()) ? bd : bd + excel.suffix();
            }
            if (StrUtil.isNotBlank(excel.readConverterExp())) {
                String[] pairs = excel.readConverterExp().split(",");
                Map<String, String> map = Arrays.stream(pairs).map(s -> s.split("="))
                        .collect(Collectors.toMap(a -> a[0], a -> a[1]));
                String strVal = String.valueOf(value);
                if (map.containsKey(strVal)) {
                    return map.get(strVal);
                }
            }
            if (StrUtil.isNotBlank(excel.suffix())) {
                return value + excel.suffix();
            }
        } catch (Exception ignored) {
        }
        return value;
    }

    /**
     * 获取列宽
     */
    public static Map<Integer, Double> buildColumnWidth(Class<?> clazz) {
        Map<Integer, Double> widthMap = new HashMap<>();
        List<Field> fields = getAllExportFields(clazz);
        int colIndex = 0;
        for (Field f : fields) {
            if (f.isAnnotationPresent(Excels.class)) {
                Excels excels = f.getAnnotation(Excels.class);
                for (Excel e : excels.value()) {
                    widthMap.put(colIndex++, e.width());
                }
            } else if (f.isAnnotationPresent(Excel.class)) {
                widthMap.put(colIndex++, f.getAnnotation(Excel.class).width());
            }
        }
        return widthMap;
    }

    /**
     * 获取表头样式
     */
    public static Map<Integer, Excel> buildHeaderStyle(Class<?> clazz) {
        Map<Integer, Excel> map = new HashMap<>();
        List<Field> fields = getAllExportFields(clazz);
        int colIndex = 0;
        for (Field f : fields) {
            if (f.isAnnotationPresent(Excels.class)) {
                Excels excels = f.getAnnotation(Excels.class);
                for (Excel e : excels.value()) {
                    map.put(colIndex++, e);
                }
            } else if (f.isAnnotationPresent(Excel.class)) {
                map.put(colIndex++, f.getAnnotation(Excel.class));
            }
        }
        return map;
    }

    /**
     * 校验是否导出字段
     *
     * @param field
     * @return
     */
    private static boolean isExportField(Field field) {
        Excel excel = field.getAnnotation(Excel.class);
        if (Objects.nonNull(excel)) {
            return excel.isExport() || Objects.equals(excel.type(), Excel.Type.ALL) || Objects.equals(excel.type(), Excel.Type.EXPORT);
        }
        return field.isAnnotationPresent(Excels.class);
    }

    /**
     * 获取排序
     *
     * @param field
     * @return
     */
    private static int getSort(Field field) {
        Excel excel = field.getAnnotation(Excel.class);
        if (excel != null) {
            return excel.sort();
        }
        Excels excels = field.getAnnotation(Excels.class);
        if (excels != null && excels.value().length > 0) {
            return excels.value()[0].sort();
        }
        return Integer.MAX_VALUE;
    }
}
