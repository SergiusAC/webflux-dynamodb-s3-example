package com.example.awssoundservice.response;

public record FileResponse (
        String fileKey,
        String fileUrl
) {
}