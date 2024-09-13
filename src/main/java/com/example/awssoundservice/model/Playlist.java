package com.example.awssoundservice.model;

import com.example.awssoundservice.common.utils.AttributeValueMapUtils;
import com.example.awssoundservice.common.utils.AttributeValueUtils;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Playlist(
        String uid,
        String name,
        List<String> trackIds
) {

    public static Playlist from(Map<String, AttributeValue> valueMap) {
        return new Playlist(
                valueMap.get("uid").s(),
                valueMap.get("name").s(),
                AttributeValueMapUtils.getStringSet(valueMap, "trackIds", Collections.emptyList())
        );
    }

    public static List<Playlist> fromList(List<Map<String, AttributeValue>> valueMaps) {
        return valueMaps.stream().map(Playlist::from).toList();
    }

    public Map<String, AttributeValue> toMap() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("uid", AttributeValue.builder().s(this.uid).build());
        map.put("name", AttributeValue.builder().s(this.name).build());
        if (this.trackIds != null && !this.trackIds.isEmpty()) {
            map.put("trackIds", AttributeValueUtils.buildStringSet(this.trackIds));
        }
        return map;
    }

}
