package com.trackingapp.tracking.controller;

import com.trackingapp.tracking.dto.TrackingRequest;
import com.trackingapp.tracking.dto.TrackingResponse;
import com.trackingapp.tracking.service.TrackingService;
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
