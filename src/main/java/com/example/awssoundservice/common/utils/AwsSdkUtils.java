package com.example.awssoundservice.common.utils;

import software.amazon.awssdk.core.SdkResponse;

import java.text.MessageFormat;
import java.util.Objects;

public class AwsSdkUtils {

    public static boolean isErrorSdkHttpResponse(SdkResponse sdkResponse) {
        return sdkResponse.sdkHttpResponse() == null || !sdkResponse.sdkHttpResponse().isSuccessful();
    }

    public static void checkSdkResponse(SdkResponse sdkResponse) {
        if (AwsSdkUtils.isErrorSdkHttpResponse(sdkResponse)){
            if (Objects.nonNull(sdkResponse.sdkHttpResponse())) {
                throw new IllegalStateException(
                        MessageFormat.format(
                                "{0} - {1}",
                                sdkResponse.sdkHttpResponse().statusCode(),
                                sdkResponse.sdkHttpResponse().statusText()
                        )
                );
            } else {
                throw new IllegalStateException("Unknown error in AWS SDK");
            }
        }
    }

}
