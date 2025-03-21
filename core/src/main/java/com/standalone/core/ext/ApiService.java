package com.standalone.core.ext;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.standalone.core.App;
import com.standalone.core.util.Json;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiService<T> {
    public static final String BASE_URL = App.loadEnv().getProperty("BASE_URL");
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;

    public ApiService() {
        this.client = new OkHttpClient.Builder()
                .callTimeout(1, TimeUnit.SECONDS)
                .build();
    }

    public CompletableFuture<Response> insert(T t) {
        ResponseFuture callback = new ResponseFuture();
        try {
            RequestBody body = RequestBody.create(Json.stringify(t), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .post(body)
                    .build();
            client.newCall(request).enqueue(callback);
        } catch (JsonProcessingException e) {
            callback.future.completeExceptionally(e);
        }

        return callback.future;
    }

    public CompletableFuture<Response> fetchAll() {
        Request request = new Request.Builder()
                .url(BASE_URL)
                .build();
        return call(request);
    }

    public CompletableFuture<Response> fetchById(long id) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/" + String.valueOf(id))
                .build();
        return call(request);
    }

    public CompletableFuture<Response> update(long id, T t) {
        ResponseFuture callback = new ResponseFuture();
        try {
            RequestBody body = RequestBody.create(Json.stringify(t), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "/" + String.valueOf(id))
                    .put(body)
                    .build();
            client.newCall(request).enqueue(callback);

        } catch (JsonProcessingException e) {
            callback.future.completeExceptionally(e);
        }
        return callback.future;

    }

    public CompletableFuture<Response> delete(long id) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/" + String.valueOf(id))
                .delete()
                .build();
        return call(request);
    }


    private CompletableFuture<Response> call(Request request) {
        ResponseFuture callback = new ResponseFuture();
        client.newCall(request).enqueue(callback);
        return callback.future;
    }

    public static CompletableFuture<List<Response>> join(List<CompletableFuture<Response>> futures) {
        CompletableFuture<Void> cfv = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        return cfv.thenApply(__ -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
    }

    public static class ResponseFuture implements Callback {
        CompletableFuture<Response> future = new CompletableFuture<>();

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            future.completeExceptionally(e);
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            future.complete(response);
        }
    }
}
