package com.lemnt.trade.adapter.web.promotion.configuration;

import com.lemnt.trade.adapter.web.promotion.req.*;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import tools.jackson.databind.*;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.LogicalType;
import tools.jackson.databind.cfg.*;
import tools.jackson.core.StreamReadFeature;
import java.util.*;
import java.io.IOException;

/** Strict only for the new promotion mutation requests; existing Jackson behavior is untouched. */
public final class PromotionJsonConverter extends AbstractHttpMessageConverter<Object> {
    private static final Set<Class<?>> TYPES =
            Set.of(
                    PromotionCreateReq.class,
                    PromotionDraftSaveReq.class,
                    PromotionPublishReq.class,
                    PromotionStateChangeReq.class);
    private final JsonMapper mapper =
            JsonMapper.builder()
                    .enable(
                            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                            DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                    .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
                    .withCoercionConfig(
                            LogicalType.Textual,
                            c ->
                                    c.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                                            .setCoercion(
                                                    CoercionInputShape.Float, CoercionAction.Fail)
                                            .setCoercion(
                                                    CoercionInputShape.Boolean,
                                                    CoercionAction.Fail))
                    .withCoercionConfig(
                            LogicalType.Integer,
                            c ->
                                    c.setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                                            .setCoercion(
                                                    CoercionInputShape.Float, CoercionAction.Fail)
                                            .setCoercion(
                                                    CoercionInputShape.Boolean,
                                                    CoercionAction.Fail))
                    .build();

    public PromotionJsonConverter() {
        super(MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
    }

    protected boolean supports(Class<?> type) {
        return TYPES.contains(type);
    }

    @Override
    public boolean canWrite(Class<?> type, MediaType media) {
        return false;
    }

    @Override
    protected Object readInternal(Class<?> type, HttpInputMessage input) throws IOException {
        try {
            var bytes = input.getBody().readNBytes(1048577);
            require(bytes.length <= 1048576);
            var node = mapper.readTree(bytes);
            if (type == PromotionCreateReq.class) fields(node, "requestNo", "code", "draft");
            else if (type == PromotionDraftSaveReq.class)
                fields(node, "requestNo", "expectedRevision", "draft");
            else if (type == PromotionPublishReq.class)
                fields(node, "requestNo", "expectedRevision");
            else fields(node, "requestNo", "expectedRevision", "action");
            if (node.has("draft")) {
                var d = node.get("draft");
                fields(
                        d,
                        "name",
                        "type",
                        "scope",
                        "audience",
                        "couponPolicy",
                        "priority",
                        "startsAtEpochMs",
                        "endsAtEpochMs",
                        "globalUseLimit",
                        "memberUseLimit",
                        "rule");
                fields(d.get("scope"), "kind", "ids");
                var rule = d.get("rule");
                if ("FULL_REDUCTION".equals(d.get("type").asString())) {
                    fields(rule, "tiers");
                    require(rule.get("tiers").isArray());
                    for (var tier : rule.get("tiers")) fields(tier, "thresholdFen", "discountFen");
                } else {
                    require("ORDER_DISCOUNT".equals(d.get("type").asString()));
                    fields(rule, "thresholdFen", "payRateBps", "maxDiscountFen");
                }
            }
            return mapper.treeToValue(node, type);
        } catch (RuntimeException ex) {
            throw new HttpMessageNotReadableException("Invalid promotion JSON", ex, input);
        }
    }

    private static void fields(JsonNode node, String... names) {
        require(
                node != null
                        && node.isObject()
                        && new HashSet<>(node.propertyNames()).equals(Set.of(names)));
    }

    private static void require(boolean valid) {
        if (!valid) throw new IllegalArgumentException("Invalid promotion structure");
    }

    @Override
    protected void writeInternal(Object value, HttpOutputMessage output) {
        throw new UnsupportedOperationException();
    }
}
