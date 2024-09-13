package com.example.awssoundservice.service;

import com.example.awssoundservice.common.DynamoDbTables;
import com.example.awssoundservice.common.utils.AwsSdkUtils;
import com.example.awssoundservice.common.utils.FileUtils;
import com.example.awssoundservice.model.Track;
import com.example.awssoundservice.request.TrackCreateOrUpdateRequest;
import com.example.awssoundservice.response.FileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackService {

    private final S3AsyncClient s3AsyncClient;
    private final DynamoDbAsyncClient dynamoDb;

    @Value("${aws.s3.track-bucket}")
    private String bucket;

    public Mono<Track> getById(String trackId) {
        GetItemRequest getItemRequest = GetItemRequest.builder()
                .tableName(DynamoDbTables.TRACKS)
                .key(Map.of("uid", AttributeValue.builder().s(trackId).build()))
                .build();
        CompletableFuture<GetItemResponse> future = dynamoDb.getItem(getItemRequest);
        return Mono.fromFuture(future)
                .map(getItemResponse -> {
                    AwsSdkUtils.checkSdkResponse(getItemResponse);
                    return Track.from(getItemResponse.item());
                });
    }

    public Mono<List<Track>> getAll() {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(DynamoDbTables.TRACKS)
                .build();
        CompletableFuture<ScanResponse> future = dynamoDb.scan(scanRequest);
        return Mono.fromFuture(future)
                .map(scanResponse -> {
                    AwsSdkUtils.checkSdkResponse(scanResponse);
                    return Track.fromList(scanResponse.items());
                });
    }

    public Mono<Track> create(TrackCreateOrUpdateRequest request) {
        Track track = new Track(
                UUID.randomUUID().toString(),
                request.name(),
                "",
                ""
        );
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(DynamoDbTables.TRACKS)
                .item(track.toMap())
                .build();
        CompletableFuture<PutItemResponse> responseFuture = dynamoDb.putItem(putItemRequest);
        return Mono.fromFuture(responseFuture)
                .map(putItemResponse -> {
                    AwsSdkUtils.checkSdkResponse(putItemResponse);
                    return track;
                });
    }

    public Mono<Track> update(String trackId, TrackCreateOrUpdateRequest request) {
        Mono<Track> trackMono = this.getById(trackId);
        return trackMono.flatMap(currentTrack -> {
            Track updatedTrack = new Track(
                    currentTrack.uid(),
                    request.name(),
                    currentTrack.fileKey(),
                    currentTrack.fileUrl()
            );
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(DynamoDbTables.TRACKS)
                    .item(updatedTrack.toMap())
                    .build();
            return Mono.fromFuture(dynamoDb.putItem(putItemRequest));
        }).flatMap(response -> {
            AwsSdkUtils.checkSdkResponse(response);
            return this.getById(trackId);
        });
    }

    public Mono<Track> delete(String trackId) {
        Mono<Track> trackMono = this.getById(trackId);
        DeleteItemRequest deleteItemRequest = DeleteItemRequest.builder()
                .tableName(DynamoDbTables.TRACKS)
                .key(Map.of("uid", AttributeValue.builder().s(trackId).build()))
                .build();
        CompletableFuture<DeleteItemResponse> deleteFuture = dynamoDb.deleteItem(deleteItemRequest);
        return trackMono.zipWith(Mono.fromFuture(deleteFuture))
                .map(objects -> {
                    Track currentTrack = objects.getT1();
                    DeleteItemResponse deleteResponse = objects.getT2();
                    AwsSdkUtils.checkSdkResponse(deleteResponse);
                    return currentTrack;
                });
    }

    public Mono<Track> uploadTrackFile(String trackId, FilePart filePart) {
        String filename = filePart.filename();
        String fileKey = trackId + "/" + filename;
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileKey)
                .contentType(Objects.requireNonNull(filePart.headers().getContentType()).toString())
                .build();
        Mono<ByteBuffer> fileByteBuffer = FileUtils.dataBuffersToByteBuffer(filePart.content());
        return fileByteBuffer
                .flatMap(buffer -> Mono.fromFuture(
                        s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromByteBuffer(buffer))
                ))
                .map(putObjectResponse -> {
                    AwsSdkUtils.checkSdkResponse(putObjectResponse);
                    GetUrlRequest getUrlRequest = GetUrlRequest.builder().bucket(bucket).key(fileKey).build();
                    String fileUrl = s3AsyncClient.utilities().getUrl(getUrlRequest).toExternalForm();
                    return new FileResponse(
                            fileKey,
                            fileUrl
                    );
                })
                .flatMap(fileResponse -> this.addFileInfoToTrack(trackId, fileResponse));
    }

    private Mono<Track> addFileInfoToTrack(String trackId, FileResponse fileResponse) {
        Mono<Track> track = this.getById(trackId);
        return track.flatMap(currentTrack -> {
            Track updatedTrack = new Track(
                    currentTrack.uid(),
                    currentTrack.name(),
                    fileResponse.fileKey(),
                    fileResponse.fileUrl()
            );
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(DynamoDbTables.TRACKS)
                    .item(updatedTrack.toMap())
                    .build();
            return Mono.fromFuture(dynamoDb.putItem(putItemRequest));
        }).flatMap(putItemResponse -> {
            AwsSdkUtils.checkSdkResponse(putItemResponse);
            return this.getById(trackId);
        });
    }

}
