package com.lemnt.common.excel.converter;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.lemnt.common.core.annotation.Excel;
import com.lemnt.common.core.annotation.Excels;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 表格导入转换器
 *
 * @author wangxx
 */
@Slf4j
public class ExcelImportAnnotationConverter {

    /**
     * 构建 Excel 列 index → Field 映射
     */
    public static Map<Integer, Field> buildHeadMap(Class<?> clazz) {
        Map<Integer, Field> map = new TreeMap<>();
        int autoIndex = 0;
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Excel excel = field.getAnnotation(Excel.class);
            if (Objects.nonNull(excel) && isImportField(excel)) {
                int index = (excel.sort() != Integer.MAX_VALUE) ? excel.sort() : autoIndex++;
                map.put(index, field);
                continue;
            }
            Excels excels = field.getAnnotation(Excels.class);
            if (Objects.nonNull(excels)) {
                for (Excel e : excels.value()) {
                    if (isImportField(e)) {
                        int index = (e.sort() != Integer.MAX_VALUE) ? e.sort() : autoIndex++;
                        map.put(index, field);
                    }
                }
            }
        }
        return map;
    }

    /**
     * 核心方法：将 Excel 数据转换为实体列表
     *
     * @param headMap 头
     * @param clazz   实体类
     * @param data    EasyExcel 读出的行数据（Map 形式，key 为列 index）
     * @param <E>     实体类型
     * @return 实体列表
     */
    public static <E> E convertRow(Map<Integer, Field> headMap, Class<E> clazz, Map<Integer, Object> data) {
        if (Objects.isNull(data)) {
            return null;
        }
        E entity = ReflectUtil.newInstance(clazz);
        for (Map.Entry<Integer, Field> entry : headMap.entrySet()) {
            Integer index = entry.getKey();
            Field targetField = entry.getValue();
            try {
                targetField.setAccessible(true);
                // 从 data 里按 index 取值
                Object cellValue = data.get(index);
                // 调用 setValue 完成类型转换和赋值
                setValue(targetField, entity, cellValue);
            } catch (Exception e) {
                log.warn("字段取值失败 field={}, index={}", targetField.getName(), index, e);
            }
        }
        return entity;
    }

    /**
     * 校验是否导入字段
     *
     * @param excel
     * @return
     */
    private static boolean isImportField(Excel excel) {
        return Objects.equals(excel.type(), Excel.Type.ALL) || Objects.equals(excel.type(), Excel.Type.IMPORT);
    }

    /**
     * 根据注解将 Excel 单元格值转换为实体字段值
     *
     * @param field     实体字段
     * @param obj       实体对象
     * @param cellValue Excel 单元格原始值
     */
    public static void setValue(Field field, Object obj, Object cellValue) {
        try {
            field.setAccessible(true);

            // 1. 多列嵌套
            if (field.isAnnotationPresent(Excels.class)) {
                Object nestedObj = field.get(obj);
                if (nestedObj == null) {
                    nestedObj = field.getType().getDeclaredConstructor().newInstance();
                    field.set(obj, nestedObj);
                }

                Excels excels = field.getAnnotation(Excels.class);
                if (cellValue instanceof List) {
                    List<?> cellValues = (List<?>) cellValue;
                    for (int i = 0; i < excels.value().length && i < cellValues.size(); i++) {
                        Excel e = excels.value()[i];
                        Field nestedField = nestedObj.getClass().getDeclaredField(e.targetAttr());
                        nestedField.setAccessible(true);
                        Object converted = processCellValue(cellValues.get(i), nestedField.getType(), e);
                        nestedField.set(nestedObj, converted);
                    }
                }
                return;
            }

            // 2. 单列
            if (field.isAnnotationPresent(Excel.class)) {
                Excel excel = field.getAnnotation(Excel.class);
                Object converted = processCellValue(cellValue, field.getType(), excel);
                field.set(obj, converted);
            }

        } catch (Exception e) {
            // 可以考虑 log.warn("字段转换失败", e);
            log.warn("字段转换失败 field={}, value={}", field.getName(), cellValue, e);
        }
    }

    /**
     * Excel 单元格值 → 字段类型值
     */
    private static Object processCellValue(Object value, Class<?> targetType, Excel excel) {
        try {
            String strVal = value == null ? "" : String.valueOf(value).trim();
            // 1. 默认值
            if (StrUtil.isEmpty(strVal) && StrUtil.isNotBlank(excel.defaultValue())) {
                strVal = excel.defaultValue();
            }
            // 2. 反解字典（readConverterExp）
            if (StrUtil.isNotBlank(excel.readConverterExp())) {
                String[] pairs = excel.readConverterExp().split(",");
                Map<String, String> map = Arrays.stream(pairs)
                        .map(s -> s.split("=")).filter(arr -> arr.length == 2)
                        .collect(Collectors.toMap(a -> a[1], a -> a[0]));
                if (!map.containsKey(strVal)) {
                    return null;
                }
                strVal = map.get(strVal);
            }
            // 3. 去掉后缀
            if (StrUtil.isNotBlank(excel.suffix()) && strVal.endsWith(excel.suffix())) {
                strVal = strVal.substring(0, strVal.length() - excel.suffix().length());
            }
            // 4. 类型转换
            if (targetType == String.class) {
                return strVal;
            }
            if (targetType == Integer.class || targetType == int.class) {
                return Integer.valueOf(strVal);
            }
            if (targetType == Long.class || targetType == long.class) {
                return Long.valueOf(strVal);
            }
            if (targetType == BigDecimal.class) {
                return new BigDecimal(strVal);
            }
            if (targetType == Double.class || targetType == double.class) {
                return Double.valueOf(strVal);
            }
            if (targetType == LocalDate.class) {
                if (StrUtil.isNotBlank(excel.dateFormat())) {
                    return LocalDate.parse(strVal, DateTimeFormatter.ofPattern(excel.dateFormat()));
                } else {
                    return LocalDate.parse(strVal);
                }
            }
            if (targetType == LocalDateTime.class) {
                if (StrUtil.isNotBlank(excel.dateFormat())) {
                    return LocalDateTime.parse(strVal, DateTimeFormatter.ofPattern(excel.dateFormat()));
                } else {
                    return LocalDateTime.parse(strVal);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

}
