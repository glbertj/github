package com.svx.github.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;

public class JsonUtility {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String serialize(Map<String, String> entries) throws JsonProcessingException {
        return mapper.writeValueAsString(entries);
    }

    public static Map<String, String> deserialize(String json) throws IOException {
        return mapper.readValue(json, new TypeReference<>() {});
    }
}