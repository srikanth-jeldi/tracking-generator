package com.trackingapp.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrackingResponse {
    private String trackingNumber;
    private String createdAt;
}
