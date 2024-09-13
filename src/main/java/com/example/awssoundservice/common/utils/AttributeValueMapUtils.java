package com.example.awssoundservice.common.utils;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class AttributeValueMapUtils {

    public static List<String> getStringSet(Map<String, AttributeValue> map, String key, Collection<String> defaultSet) {
        return map.getOrDefault(
                key,
                AttributeValueUtils.buildStringSet(defaultSet)
        ).ss();
    }

}
