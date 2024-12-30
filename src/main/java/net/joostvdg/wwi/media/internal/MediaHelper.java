package net.joostvdg.wwi.media.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.JSONB;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MediaHelper {

    private MediaHelper() {
        throw new IllegalStateException("Utility class");
    }

    public static Optional<Map<String, String>> translateTags(JSONB tagsFromDatabase) {
        Map<String, String> tags = new HashMap<>();
        String jsonData = tagsFromDatabase.data();

        if (!jsonData.isBlank()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                tags = objectMapper.readValue(jsonData, new TypeReference<Map<String,String>>(){});
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }


        return Optional.of(tags);
    }

    public static String translateTagsToJson(Map<String, String> tags) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String translateSeanonsToJson(Map<String, Integer> seasons) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(seasons);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Integer> translateJsonToSeasons(String jsonData) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonData, new TypeReference<Map<String,Integer>>(){});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
