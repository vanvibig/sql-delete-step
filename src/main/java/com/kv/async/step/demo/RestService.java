package com.kv.async.step.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class RestService {

    @Autowired
    RestTemplate restTemplate;

    public void pingGoogle() {
        var result = restTemplate.getForObject("https://httpbin.org/get", String.class);
        log.info(result);
    }
}
