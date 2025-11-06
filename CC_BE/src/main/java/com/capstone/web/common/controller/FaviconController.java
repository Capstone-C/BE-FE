package com.capstone.web.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FaviconController {

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        // Avoid 500 when browsers request /favicon.ico; return 204 No Content
        return ResponseEntity.noContent().build();
    }
}

