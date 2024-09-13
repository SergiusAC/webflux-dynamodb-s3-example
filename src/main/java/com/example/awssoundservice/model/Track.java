package com.example.awssoundservice.model;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;

public record Track(
        String uid,
        String name,
        String fileKey,
        String fileUrl
) {
    public static Track from(Map<String, AttributeValue> valueMap) {
        return new Track(
                valueMap.get("uid").s(),
                valueMap.get("name").s(),
                valueMap.get("fileKey").s(),
                valueMap.get("fileUrl").s()
        );
    }

    public static List<Track> fromList(List<Map<String, AttributeValue>> valueMaps) {
        return valueMaps.stream().map(Track::from).toList();
    }

    public Map<String, AttributeValue> toMap() {
        return Map.of(
                "uid", AttributeValue.builder().s(this.uid).build(),
                "name", AttributeValue.builder().s(this.name).build(),
                "fileKey", AttributeValue.builder().s(this.fileKey).build(),
                "fileUrl", AttributeValue.builder().s(this.fileUrl).build()
        );
    }
}
