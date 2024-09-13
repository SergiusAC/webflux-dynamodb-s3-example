package com.example.awssoundservice.controller;

import com.example.awssoundservice.model.Track;
import com.example.awssoundservice.request.TrackCreateOrUpdateRequest;
import com.example.awssoundservice.response.GeneralResponse;
import com.example.awssoundservice.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/tracks")
@RequiredArgsConstructor
public class TrackController {

    private final TrackService trackService;

    @GetMapping
    public Mono<GeneralResponse<List<Track>>> getAll() {
        return trackService.getAll().map(GeneralResponse::successResponse);
    }

    @GetMapping("/{uid}")
    public Mono<GeneralResponse<Track>> getById(@PathVariable String uid) {
        return trackService.getById(uid).map(GeneralResponse::successResponse);
    }

    @PostMapping
    public Mono<GeneralResponse<Track>> create(@RequestBody TrackCreateOrUpdateRequest request) {
        return trackService.create(request).map(GeneralResponse::successResponse);
    }

    @PutMapping("/{uid}")
    public Mono<GeneralResponse<Track>> update(@PathVariable String uid, @RequestBody TrackCreateOrUpdateRequest request) {
        return trackService.update(uid, request).map(GeneralResponse::successResponse);
    }

    @DeleteMapping("/{uid}")
    public Mono<GeneralResponse<Track>> delete(@PathVariable String uid) {
        return trackService.delete(uid).map(GeneralResponse::successResponse);
    }

    @PostMapping("/{uid}/upload")
    public Mono<GeneralResponse<Track>> uploadTrackFile(
            @PathVariable String uid, @RequestPart(name = "file") Mono<FilePart> file
    ) {
        return file
                .flatMap(filePart -> trackService.uploadTrackFile(uid, filePart))
                .map(GeneralResponse::successResponse);
    }

}
