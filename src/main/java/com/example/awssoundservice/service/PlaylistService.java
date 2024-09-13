package com.example.awssoundservice.service;

import com.example.awssoundservice.common.DynamoDbTables;
import com.example.awssoundservice.common.utils.AwsSdkUtils;
import com.example.awssoundservice.model.Playlist;
import com.example.awssoundservice.model.Track;
import com.example.awssoundservice.request.PlaylistAddTrackRequest;
import com.example.awssoundservice.request.PlaylistCreateOrUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PlaylistService {

    private final DynamoDbAsyncClient dynamoDb;
    private final TrackService trackService;

    public Mono<Playlist> findById(String uid) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(DynamoDbTables.PLAYLISTS)
                .key(Map.of("uid", AttributeValue.builder().s(uid).build()))
                .build();
        CompletableFuture<GetItemResponse> responseFuture = dynamoDb.getItem(request);
        return Mono.fromFuture(responseFuture)
                .map(response -> {
                    Map<String, AttributeValue> item = response.item();
                    if (item.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found");
                    }
                    return item;
                })
                .map(Playlist::from);
    }

    public Mono<List<Playlist>> findAll() {
        ScanRequest request = ScanRequest.builder().tableName(DynamoDbTables.PLAYLISTS).build();
        CompletableFuture<ScanResponse> responseFuture = dynamoDb.scan(request);
        return Mono.fromFuture(responseFuture)
                .map(ScanResponse::items)
                .map(Playlist::fromList);
    }

    public Mono<Playlist> create(PlaylistCreateOrUpdateRequest request) {
        Playlist playlist = new Playlist(
                UUID.randomUUID().toString(),
                request.name(),
                request.trackIds()
        );
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(DynamoDbTables.PLAYLISTS)
                .item(playlist.toMap())
                .build();
        CompletableFuture<PutItemResponse> responseFuture = dynamoDb.putItem(putItemRequest);
        return Mono.fromFuture(responseFuture).map(response -> {
            AwsSdkUtils.checkSdkResponse(response);
            return playlist;
        });
    }

    public Mono<Playlist> update(String uid, PlaylistCreateOrUpdateRequest request) {
        Mono<Playlist> playlist = this.findById(uid);
        return playlist.flatMap(currentPlaylist -> {
            Playlist updatedPlaylist = new Playlist(
                    currentPlaylist.uid(),
                    request.name(),
                    request.trackIds()
            );
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(DynamoDbTables.PLAYLISTS)
                    .item(updatedPlaylist.toMap())
                    .build();
            CompletableFuture<PutItemResponse> responseFuture = dynamoDb.putItem(putItemRequest);
            return Mono.fromFuture(responseFuture).map(response -> {
                AwsSdkUtils.checkSdkResponse(response);
                return updatedPlaylist;
            });
        });
    }

    public Mono<Playlist> addTrackToPlaylist(PlaylistAddTrackRequest request) {
        Mono<Playlist> playlist = this.findById(request.playlistId());
        Mono<Track> track = trackService.getById(request.trackId());
        return playlist.zipWith(track).flatMap(playlistAndTrack -> {
            Playlist currentPlaylist = playlistAndTrack.getT1();
            Track trackToAdd = playlistAndTrack.getT2();
            ArrayList<String> newTrackIds = new ArrayList<>(currentPlaylist.trackIds());
            newTrackIds.add(trackToAdd.uid());
            PlaylistCreateOrUpdateRequest updateRequest = new PlaylistCreateOrUpdateRequest(
                    currentPlaylist.name(),
                    newTrackIds
            );
            return this.update(currentPlaylist.uid(), updateRequest);
        });
    }

    public Mono<Playlist> delete(String uid) {
        Mono<Playlist> currentPlaylistMono = this.findById(uid.strip());
        return currentPlaylistMono.flatMap(currentPlaylist -> {
            DeleteItemRequest request = DeleteItemRequest.builder()
                    .tableName(DynamoDbTables.PLAYLISTS)
                    .key(Map.of("uid", AttributeValue.builder().s(currentPlaylist.uid()).build()))
                    .build();
            CompletableFuture<DeleteItemResponse> responseFuture = dynamoDb.deleteItem(request);
            return Mono.fromFuture(responseFuture).map(__ -> currentPlaylist);
        });
    }

    public Mono<Boolean> existsById(String uid) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(DynamoDbTables.PLAYLISTS)
                .key(Map.of("uid", AttributeValue.builder().s(uid).build()))
                .build();
        CompletableFuture<GetItemResponse> responseFuture = dynamoDb.getItem(request);
        return Mono.fromFuture(responseFuture)
                .map(GetItemResponse::item)
                .map(item -> !item.isEmpty());
    }

    public Mono<List<Track>> getTracksByPlaylistId(String playlistId) {
        Mono<Playlist> playlistMono = this.findById(playlistId);
        return playlistMono.flatMap(playlist -> {
            List<String> trackIds = playlist.trackIds();
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName(DynamoDbTables.TRACKS)
                    .filterExpression(this.buildFilterExpression(trackIds))
                    .expressionAttributeValues(this.buildFilterExpressionValues(trackIds))
                    .build();
            return Mono.fromFuture(dynamoDb.scan(scanRequest));
        }).map(scanResponse -> {
            AwsSdkUtils.checkSdkResponse(scanResponse);
            List<Map<String, AttributeValue>> items = scanResponse.items();
            return Track.fromList(items);
        });
    }

    private String buildFilterExpression(List<String> trackIds) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < trackIds.size(); i++) {
            names.add(":trackId%d".formatted(i + 1));
        }
        String namesJoined = String.join(", ", names);
        return "uid IN (%s)".formatted(namesJoined);
    }

    private Map<String, AttributeValue> buildFilterExpressionValues(List<String> trackIds) {
        Map<String, AttributeValue> result = new HashMap<>();
        for (int i = 0; i < trackIds.size(); i++) {
            String name = ":trackId%d".formatted(i + 1);
            String value = trackIds.get(i);
            result.put(name, AttributeValue.builder().s(value).build());
        }
        return result;
    }

}
