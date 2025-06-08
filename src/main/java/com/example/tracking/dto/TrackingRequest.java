package com.example.tracking.dto;

import lombok.Data;

@Data
public class TrackingRequest {
    private String originCountryId;
    private String destinationCountryId;
    private Double weight;
    private String createdAt;
    private String customerId;
    private String customerName;
    private String customerSlug;
}
