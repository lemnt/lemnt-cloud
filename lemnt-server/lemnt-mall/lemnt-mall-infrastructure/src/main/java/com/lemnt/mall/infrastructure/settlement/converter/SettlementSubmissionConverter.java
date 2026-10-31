package com.lemnt.mall.infrastructure.settlement.converter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/** 旧提交回执只转换原商品规格，不改写恢复索引或查询当前商品。 */
public final class SettlementSubmissionConverter {
    private SettlementSubmissionConverter() {}

    /** 旧名值对象转规格列表，缺失编号保持为空。 */
    public static String normalize(String json) {
        var root = JSON.parseObject(json);
        var order = root.getJSONObject("order");
        var items = order == null ? null : order.getJSONArray("items");
        if (items == null) {
            return json;
        }
        boolean changed = false;
        for (Object value : items) {
            var item = (JSONObject) value;
            if (item.get("properties") instanceof JSONObject legacy) {
                var properties = new JSONArray();
                legacy.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).forEach(entry -> {
                    if (!(entry.getValue() instanceof String)) {
                        throw new IllegalStateException("旧提交回执规格格式不合法");
                    }
                    var property = new JSONObject();
                    property.put("propertyName", entry.getKey());
                    property.put("valueName", entry.getValue());
                    properties.add(property);
                });
                item.put("properties", properties);
                changed = true;
            }
        }
        return changed ? root.toJSONString() : json;
    }
}
