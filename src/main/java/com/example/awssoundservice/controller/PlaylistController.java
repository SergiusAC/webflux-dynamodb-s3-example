package com.example.awssoundservice.controller;

import com.example.awssoundservice.model.Track;
import com.example.awssoundservice.request.PlaylistAddTrackRequest;
import com.example.awssoundservice.request.PlaylistCreateOrUpdateRequest;
import com.example.awssoundservice.response.GeneralResponse;
import com.example.awssoundservice.model.Playlist;
import com.example.awssoundservice.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping
    public Mono<GeneralResponse<List<Playlist>>> getAll() {
        return playlistService.findAll().map(GeneralResponse::successResponse);
    }

    @GetMapping("/{uid}")
    public Mono<GeneralResponse<Playlist>> getById(@PathVariable String uid) {
        return playlistService.findById(uid).map(GeneralResponse::successResponse);
    }

    @GetMapping("/{uid}/exists")
    public Mono<GeneralResponse<Boolean>> existsById(@PathVariable String uid) {
        return playlistService.existsById(uid).map(GeneralResponse::successResponse);
    }

    @PostMapping
    public Mono<GeneralResponse<Playlist>> create(@RequestBody PlaylistCreateOrUpdateRequest request) {
        return playlistService.create(request).map(GeneralResponse::successResponse);
    }

    @PutMapping("/{uid}")
    public Mono<GeneralResponse<Playlist>> update(
            @PathVariable String uid, @RequestBody PlaylistCreateOrUpdateRequest request
    ) {
        return playlistService.update(uid, request).map(GeneralResponse::successResponse);
    }

    @PutMapping("/addTrack")
    public Mono<GeneralResponse<Playlist>> addTrack(@RequestBody PlaylistAddTrackRequest request) {
        return playlistService.addTrackToPlaylist(request).map(GeneralResponse::successResponse);
    }

    @DeleteMapping("/{uid}")
    public Mono<GeneralResponse<Playlist>> delete(@PathVariable String uid) {
        return playlistService.delete(uid).map(GeneralResponse::successResponse);
    }

    @GetMapping("/{uid}/tracks")
    public Mono<GeneralResponse<List<Track>>> getTracksByPlaylistId(@PathVariable String uid) {
        return playlistService.getTracksByPlaylistId(uid).map(GeneralResponse::successResponse);
    }

}
