package kr.ac.knu.calendar.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class DataLoader {
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * HTTP 요청을 전송하고 응답 바디를 받습니다.
     * @param Url 요청 URL
     * @return 응답 (body)
     * @throws RuntimeException URL이 잘못되었거나 요청이 실패함
     */
    public static String loadData(String Url) throws RuntimeException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(Url))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != HttpURLConnection.HTTP_OK)
                throw new RuntimeException("정보를 불러올 수 없습니다. HTTP 상태 " + response.statusCode());

            return response.body();
        } catch (IOException e) {
            throw new RuntimeException("정보를 불러올 수 없습니다: " + e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException("정보 요청이 취소되었습니다.");
        }
    }
}