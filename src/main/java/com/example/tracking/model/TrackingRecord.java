package com.example.tracking.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "tracking_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackingRecord {

    @Id
    private String id;

    private String trackingNumber;  // <-- Make sure this field exists with this exact name

    private String originCountryId;
    private String destinationCountryId;
    private Double weight;
    private Instant createdAt;

    private String customerId;
    private String customerName;
    private String customerSlug;
}
