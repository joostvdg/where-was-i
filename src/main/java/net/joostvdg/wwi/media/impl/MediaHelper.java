package net.joostvdg.wwi.media.impl;

import com.alibaba.fastjson.JSON;
import org.jooq.JSONB;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class MediaHelper {

    static Optional<Map<String, String>> translateTags(JSONB tagsFromDatabase) {
        Map<String, String> tags = new HashMap<>();
        String jsonData = tagsFromDatabase.data();
        if (!jsonData.isBlank()) {
            tags = JSON.parseObject(jsonData, Map.class);
        }
        return Optional.of(tags);
    }
}
