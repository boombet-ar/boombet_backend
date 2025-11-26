package com.boombet.boombet_backend.controller;

import com.boombet.boombet_backend.dto.WebsocketDTO;

import com.boombet.boombet_backend.service.WebsocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebsocketController {

    private final WebsocketService webSocketService;

    @PostMapping("/notify-socket")
    public ResponseEntity<Void> triggerWebSocket(@RequestBody WebsocketDTO request) {

        webSocketService.sendToWebSocket(request);

        return ResponseEntity.ok().build();
    }
}