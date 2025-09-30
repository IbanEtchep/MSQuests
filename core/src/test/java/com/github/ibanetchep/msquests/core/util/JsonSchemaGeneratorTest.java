package com.github.ibanetchep.msquests.core.util;

import com.github.ibanetchep.msquests.core.util.testmodels.DummyConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonSchemaGeneratorTest {

    @Test
    void generateSchema() {
        String schema = JsonSchemaGenerator.generateSchema(DummyConfig.class);

        assertNotNull(schema);
        assertTrue(schema.contains("\"foo\""));
        assertTrue(schema.contains("\"bar\""));
        assertTrue(schema.contains("\"baz\""));
        assertTrue(schema.contains("\"anyOf\""));
        System.out.println("Generated schema:\n" + schema);
    }

}