package com.example.tracking.controller;

import com.example.tracking.dto.TrackingRequest;
import com.example.tracking.dto.TrackingResponse;
import com.example.tracking.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @GetMapping("/next-tracking-number")
    public TrackingResponse getNextTrackingNumber(@ModelAttribute TrackingRequest request) {
        return trackingService.generateTrackingNumber(request);
    }
    }
