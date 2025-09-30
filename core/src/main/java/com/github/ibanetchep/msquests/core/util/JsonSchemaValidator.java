package com.github.ibanetchep.msquests.core.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.util.Set;

public class JsonSchemaValidator {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void validate(Object data, String schemaJson) {
        try {
            JsonNode schemaNode = MAPPER.readTree(schemaJson);
            JsonSchema schema = JsonSchemaFactory
                    .getInstance(SpecVersion.VersionFlag.V202012)
                    .getSchema(schemaNode);

            JsonNode dataNode = MAPPER.valueToTree(data);
            Set<ValidationMessage> errors = schema.validate(dataNode);

            if (!errors.isEmpty()) {
                StringBuilder sb = new StringBuilder("Schema validation failed:\n");
                for (ValidationMessage error : errors) {
                    sb.append(" - ").append(error.getMessage()).append("\n");
                }
                throw new IllegalArgumentException(sb.toString());
            }
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) throw (IllegalArgumentException) e;
            throw new RuntimeException("Schema validation error", e);
        }
    }

}
