package com.github.ibanetchep.msquests.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.ibanetchep.msquests.core.quest.config.annotation.AtLeastOneOfFields;
import com.github.ibanetchep.msquests.core.quest.config.annotation.ConfigField;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class JsonSchemaGenerator {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String generateSchema(Class<?> configClass) {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("type", "object");

        ObjectNode properties = MAPPER.createObjectNode();
        List<String> required = new ArrayList<>();

        // -----------------------------
        // Fields
        // -----------------------------
        for (Field field : configClass.getDeclaredFields()) {
            ConfigField cf = field.getAnnotation(ConfigField.class);
            if (cf == null) continue;

            ObjectNode prop = MAPPER.createObjectNode();

            Class<?> type = field.getType();
            if (type == String.class) {
                prop.put("type", "string");
            } else if (Number.class.isAssignableFrom(type) || (type.isPrimitive() && type != boolean.class)) {
                prop.put("type", "integer");
            } else if (type == boolean.class || type == Boolean.class) {
                prop.put("type", "boolean");
            } else if (type.isEnum()) {
                prop.put("type", "string");
                var values = MAPPER.createArrayNode();
                for (Object c : type.getEnumConstants()) {
                    values.add(c.toString());
                }
                prop.set("enum", values);
            } else {
                prop.put("type", "string"); // fallback
            }

            properties.set(cf.name(), prop);

            if (cf.required()) {
                required.add(cf.name());
            }
        }

        root.set("properties", properties);
        if (!required.isEmpty()) {
            root.set("required", MAPPER.valueToTree(required));
        }

        // -----------------------------
        // @AtLeastOneOfFields
        // -----------------------------
        AtLeastOneOfFields[] groups = configClass.getAnnotationsByType(AtLeastOneOfFields.class);
        if (groups.length > 0) {
            var allOf = MAPPER.createArrayNode();

            for (AtLeastOneOfFields group : groups) {
                var anyOf = MAPPER.createArrayNode();
                for (String field : group.value()) {
                    ObjectNode sub = MAPPER.createObjectNode();
                    sub.putArray("required").add(field);
                    anyOf.add(sub);
                }
                ObjectNode wrapper = MAPPER.createObjectNode();
                wrapper.set("anyOf", anyOf);
                allOf.add(wrapper);
            }

            root.set("allOf", allOf);
        }

        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate schema", e);
        }
    }
}