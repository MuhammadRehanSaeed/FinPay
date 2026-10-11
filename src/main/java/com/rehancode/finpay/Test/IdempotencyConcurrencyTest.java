package com.rehancode.finpay.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class IdempotencyConcurrencyTest {

    private static final String URL =
            "http://localhost:8080/api/v1/transaction/transfer";

    private static final String TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJpZCI6MSwic3ViIjoicmVoYW4iLCJpYXQiOjE3OTA1MjIxNTMsImp0aSI6IjEyNTgyY2RhLWU0MzgtNGFjOS1iMTE2LTBjZTg4YjM0NzViYiIsImV4cCI6MTc5MDUyMzk1M30.OGt1O_kmlv-uyYpmrZe9-sgehiNM1aI_x7e80tzaB5r_0X9WXCbItSzPjijvE35w";

    public static void main(String[] args) {

        HttpClient client = HttpClient.newHttpClient();

        String idempotencyKey = "CONCURRENT-" + System.currentTimeMillis();

        String body = """
                {
                    "receiverUserId": 4,
                    "amount": 100
                }
                """;

        HttpRequest request1 = buildRequest(body, idempotencyKey);
        HttpRequest request2 = buildRequest(body, idempotencyKey);

        CompletableFuture<HttpResponse<String>> requestA =
                CompletableFuture.supplyAsync(() -> send(client, request1));

        CompletableFuture<HttpResponse<String>> requestB =
                CompletableFuture.supplyAsync(() -> send(client, request2));

        CompletableFuture.allOf(requestA, requestB).join();

        System.out.println("========== REQUEST A ==========");
        System.out.println(requestA.join().statusCode());
        System.out.println(requestA.join().body());

        System.out.println("========== REQUEST B ==========");
        System.out.println(requestB.join().statusCode());
        System.out.println(requestB.join().body());
    }

    private static HttpRequest buildRequest(
            String body,
            String idempotencyKey) {

        return HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + TOKEN)
                .header("Idempotency-Key", idempotencyKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }

    private static HttpResponse<String> send(
            HttpClient client,
            HttpRequest request) {

        try {
            return client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
