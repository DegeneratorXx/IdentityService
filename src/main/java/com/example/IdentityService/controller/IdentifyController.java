package com.example.IdentityService.controller;

import com.example.IdentityService.dto.IdentifyRequest;
import com.example.IdentityService.dto.IdentifyResponse;
import com.example.IdentityService.service.IdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/identify")
@RequiredArgsConstructor
public class IdentifyController {

    private final IdentityService identityService;

    @PostMapping
    public ResponseEntity<IdentifyResponse> identify(
            @RequestBody IdentifyRequest request) {

        IdentifyResponse response = identityService.identify(request);

        return ResponseEntity.ok(response);
    }
}