package com.lemnt.product.infrastructure.product.converter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * 持久化边界统一规格JSON，只转换保存的旧字段，不查询字典或推测历史编号。
 */
public final class SpecSnapshotJson {
    private SpecSnapshotJson() {
    }

    public static <T> List<T> read(String json, Class<T> type) {
        return json == null
            ? null
            : JSON.parseArray(JSON.toJSONString(normalize(JSON.parse(json))), type);
    }

    /**
     * 兼容旧四字段列表和更早的名值对象，保留未知null与已知空数组。
     */
    public static Object normalize(Object raw) {
        if (raw == null) return null;
        if (raw instanceof JSONObject legacy) {
            var result = new JSONArray();
            legacy.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(
                    entry -> {
                        if (!(entry.getValue() instanceof String))
                            throw new IllegalStateException("历史规格格式不合法");
                        var spec = new JSONObject();
                        spec.put("specName", entry.getKey());
                        spec.put("specValueName", entry.getValue());
                        result.add(spec);
                    });
            return result;
        }
        if (!(raw instanceof JSONArray rows)) throw new IllegalStateException("规格必须为数组");
        for (Object value : rows) {
            if (!(value instanceof JSONObject spec)) throw new IllegalStateException("规格项必须为对象");
            rename(spec, "propertyId", "specId");
            rename(spec, "propertyName", "specName");
            rename(spec, "valueId", "specValueId");
            rename(spec, "valueName", "specValueName");
        }
        return rows;
    }

    private static void rename(JSONObject spec, String oldName, String name) {
        if (spec.containsKey(oldName)) {
            if (!spec.containsKey(name)) spec.put(name, spec.get(oldName));
            spec.remove(oldName);
        }
    }
}
