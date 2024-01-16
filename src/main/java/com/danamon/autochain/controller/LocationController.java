package com.danamon.autochain.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @GetMapping(path = "/provinces", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getProvinces() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForEntity("https://emsifa.github.io/api-wilayah-indonesia/api/provinces.json", String.class);
    }

    @GetMapping(path = "/city/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCity(@PathVariable String id) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForEntity("https://emsifa.github.io/api-wilayah-indonesia/api/regencies/" + id + ".json", String.class);
    }
}
