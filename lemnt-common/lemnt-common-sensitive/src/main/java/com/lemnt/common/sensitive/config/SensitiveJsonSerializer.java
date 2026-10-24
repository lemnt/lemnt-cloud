package com.lemnt.common.sensitive.config;

import java.util.Objects;
import java.util.Set;

import com.lemnt.common.security.enums.RoleKeyEnum;
import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.common.sensitive.annotation.Sensitive;
import com.lemnt.common.sensitive.enums.DesensitizedType;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * 数据脱敏序列化过滤
 *
 * @author wangxx
 */
public class SensitiveJsonSerializer extends ValueSerializer<String> {
    private DesensitizedType desensitizedType;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializationContext serializers) {
        if (desensitization()) {
            gen.writeString(desensitizedType.desensitizer().apply(value));
        } else {
            gen.writeString(value);
        }
    }

    @Override
    public ValueSerializer<?> createContextual(SerializationContext prov, BeanProperty property) {
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            this.desensitizedType = annotation.desensitizedType();
            return this;
        }
        return prov.findValueSerializer(property.getType());
    }

    /**
     * 是否需要脱敏处理
     */
    private boolean desensitization() {
        try {
            Set<String> roleKeys = SecurityContextHolder.getRoleKey();
            // 管理员不脱敏
            return !roleKeys.contains(RoleKeyEnum.ADMIN.getRoleKey()) && !roleKeys.contains(RoleKeyEnum.SYSTEM.getRoleKey());
        } catch (Exception e) {
            return true;
        }
    }
}
