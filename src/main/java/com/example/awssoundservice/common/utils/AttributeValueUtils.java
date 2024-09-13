package com.example.awssoundservice.common.utils;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Collection;

public class AttributeValueUtils {

    public static AttributeValue buildStringSet(Collection<String> stringSet) {
        return AttributeValue.builder().ss(stringSet).build();
    }

}
