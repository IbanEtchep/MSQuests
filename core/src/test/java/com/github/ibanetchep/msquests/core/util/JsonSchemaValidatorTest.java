package com.github.ibanetchep.msquests.core.util;

import com.github.ibanetchep.msquests.core.util.testmodels.DummyConfig;
import com.github.ibanetchep.msquests.core.util.testmodels.DummyMultiGroupConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonSchemaValidatorTest {

    @Test
    void validateSchema() {
        String schema = JsonSchemaGenerator.generateSchema(DummyConfig.class);

        // Cas valide
        Map<String, Object> valid = Map.of(
                "foo", "hello",
                "baz", true
        );

        assertDoesNotThrow(() -> JsonSchemaValidator.validate(valid, schema));

        // Cas invalide : baz manquant
        Map<String, Object> missingBaz = Map.of("foo", "hello");
        assertThrows(IllegalArgumentException.class, () ->
                JsonSchemaValidator.validate(missingBaz, schema)
        );

        // Cas invalide : foo et bar manquants
        Map<String, Object> missingFooBar = Map.of("baz", true);
        assertThrows(IllegalArgumentException.class, () ->
                JsonSchemaValidator.validate(missingFooBar, schema)
        );
    }

    @Test
    void validateSchemaWithMultipleGroups() {
        String schema = JsonSchemaGenerator.generateSchema(DummyMultiGroupConfig.class);

        // ✅ Cas valide : foo + baz + title
        Map<String, Object> valid = Map.of(
                "foo", "hello",
                "baz", true,
                "title", "hello title"
        );
        assertDoesNotThrow(() -> JsonSchemaValidator.validate(valid, schema));

        // ❌ Cas invalide : manque foo/bar et title/title_key
        Map<String, Object> invalid = Map.of(
                "baz", true
        );
        assertThrows(IllegalArgumentException.class, () ->
                JsonSchemaValidator.validate(invalid, schema)
        );
    }
}