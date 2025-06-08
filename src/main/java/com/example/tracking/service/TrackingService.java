package com.example.tracking.service;

import com.example.tracking.dto.TrackingRequest;
import com.example.tracking.dto.TrackingResponse;
import com.example.tracking.model.TrackingRecord;
import com.example.tracking.repository.TrackingRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingService {

    private final TrackingRepository trackingRepository;
    private final MeterRegistry meterRegistry;
    private static final SecureRandom random = new SecureRandom();
    private static final int MAX_ATTEMPTS = 5;

    public TrackingResponse generateTrackingNumber(TrackingRequest request) {
        log.info("Received request to generate tracking number: {}", request);
        validateRequest(request);

        String trackingNumber = createTrackingNumber(request);
        log.info("Generated tracking number: {}", trackingNumber);

        TrackingRecord record = new TrackingRecord();
        record.setOriginCountryId(request.getOriginCountryId());
        record.setDestinationCountryId(request.getDestinationCountryId());
        record.setWeight(request.getWeight());
        record.setCreatedAt(Instant.parse(request.getCreatedAt()));
        record.setCustomerId(request.getCustomerId());
        record.setCustomerName(request.getCustomerName());
        record.setCustomerSlug(request.getCustomerSlug());
        record.setTrackingNumber(trackingNumber);

        trackingRepository.save(record);
        log.info("Tracking record saved to MongoDB: {}", record);

        return new TrackingResponse(trackingNumber, Instant.now().toString());
    }

    private String createTrackingNumber(TrackingRequest request) {
        String prefix = request.getOriginCountryId().toUpperCase() + request.getDestinationCountryId().toUpperCase();
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String randomPart = generateRandomString(12 - prefix.length());
            String trackingNumber = (prefix + randomPart).toUpperCase();

            if (isValidTrackingNumber(trackingNumber)) {
                boolean exists = trackingRepository.existsByTrackingNumber(trackingNumber);
                if (!exists) {
                    meterRegistry.counter("tracking.number.generated", "origin", request.getOriginCountryId(), "destination", request.getDestinationCountryId()).increment();
                    log.info("Tracking number is unique and valid: {}", trackingNumber);
                    return trackingNumber;
                } else {
                    meterRegistry.counter("tracking.number.collision").increment();
                    log.warn("Tracking number collision detected: {}. Retrying...", trackingNumber);
                }
            }
        }
        meterRegistry.counter("tracking.number.failure").increment();
        log.error("Failed to generate a unique tracking number after {} attempts", MAX_ATTEMPTS);
        throw new IllegalStateException("Unable to generate unique tracking number");
    }

    private void validateRequest(TrackingRequest request) {
        if (request.getOriginCountryId() == null || request.getDestinationCountryId() == null ||
                request.getCustomerId() == null || request.getCustomerSlug() == null) {
            log.error("Invalid request received: {}", request);
            throw new IllegalArgumentException("Missing required fields in request");
        }
        log.info("Request validated successfully: {}", request);
    }

    private String generateRandomString(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        String result = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, length);
        log.debug("Generated random string: {}", result);
        return result;
    }

    private boolean isValidTrackingNumber(String trackingNumber) {
        boolean valid = trackingNumber.matches("^[A-Z0-9]{1,16}$");
        log.debug("Tracking number {} is valid: {}", trackingNumber, valid);
        return valid;
    }
}
