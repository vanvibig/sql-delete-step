package com.kv.async.step.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StopWatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest req, byte[] reqBody, ClientHttpRequestExecution ex) throws IOException {
        log.debug("Request body: {}", new String(reqBody, StandardCharsets.UTF_8));
        var stopWatch = new StopWatch();

        stopWatch.start();
        var response = ex.execute(req, reqBody);
        stopWatch.stop();

        var isr = new InputStreamReader(response.getBody(), StandardCharsets.UTF_8);
        var body = new BufferedReader(isr).lines().collect(Collectors.joining("\n"));
        log.debug("Response body in {}: {}", stopWatch.getTotalTimeMillis(), body);
        return response;
    }
}