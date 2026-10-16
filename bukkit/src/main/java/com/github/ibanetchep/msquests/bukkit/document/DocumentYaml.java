package com.github.ibanetchep.msquests.bukkit.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** YAML as the editor writes it: no document marker, minimal quotes, block scalars for multi-line text. */
final class DocumentYaml {

    static final ObjectMapper MAPPER = new ObjectMapper(YAMLFactory.builder()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            .enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
            .enable(YAMLGenerator.Feature.LITERAL_BLOCK_STYLE)
            .enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR)
            .disable(YAMLGenerator.Feature.SPLIT_LINES)
            .build())
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private static final TypeReference<LinkedHashMap<String, Object>> TREE = new TypeReference<>() {
    };

    private DocumentYaml() {
    }

    static Map<String, Object> parse(String yaml) throws IOException {
        Map<String, Object> tree = MAPPER.readValue(yaml, TREE);
        return tree != null ? tree : new LinkedHashMap<>();
    }

    static String write(Map<String, Object> tree) throws IOException {
        return MAPPER.writeValueAsString(tree);
    }

    /** A DTO as a plain tree (actions, conditions, objectives keep their own fields). */
    static Map<String, Object> toTree(Object value) {
        return MAPPER.convertValue(value, TREE);
    }

    static <T> T fromTree(Object tree, Class<T> type) {
        return MAPPER.convertValue(tree, type);
    }
}
