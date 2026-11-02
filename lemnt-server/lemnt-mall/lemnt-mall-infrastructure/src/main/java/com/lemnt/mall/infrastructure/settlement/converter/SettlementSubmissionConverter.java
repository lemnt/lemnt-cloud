package com.lemnt.mall.infrastructure.settlement.converter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.lemnt.mall.infrastructure.product.converter.SpecSnapshotJson;

/** 提交回执只规范原商品规格，不改写恢复索引、原指纹或查询当前商品。 */
public final class SettlementSubmissionConverter {
    private SettlementSubmissionConverter() { }

    public static String normalize(String json) {
        var root = JSON.parseObject(json);
        var order = root.getJSONObject("order");
        var items = order == null ? null : order.getJSONArray("items");
        if (items == null) return json;
        for (Object value : items) {
            var item = (JSONObject) value;
            Object specs = item.containsKey("specs") ? item.get("specs") : item.get("properties");
            if (item.containsKey("specs") || item.containsKey("properties")) {
                item.put("specs", SpecSnapshotJson.normalize(specs));
                item.remove("properties");
            }
        }
        return root.toJSONString();
    }
}
