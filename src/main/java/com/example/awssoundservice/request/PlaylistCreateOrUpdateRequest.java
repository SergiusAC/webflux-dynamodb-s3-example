package com.example.awssoundservice.request;

import java.util.List;

public record PlaylistCreateOrUpdateRequest(String name, List<String> trackIds) {
}
