package com.trackingapp.tracking.service;

import com.trackingapp.tracking.dto.TrackingRequest;
import com.trackingapp.tracking.dto.TrackingResponse;
import com.trackingapp.tracking.exception.InvalidTrackingRequestException;
import com.trackingapp.tracking.exception.TrackingNumberGenerationException;
import com.trackingapp.tracking.model.TrackingRecord;
import com.trackingapp.tracking.repository.TrackingRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.format.DateTimeParseException;
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
        try {
            validateRequest(request);
            String trackingNumber = createTrackingNumber(request);
            log.info("Generated tracking number: {}", trackingNumber);

            TrackingRecord record = new TrackingRecord();
            record.setOriginCountryId(request.getOriginCountryId());
            record.setDestinationCountryId(request.getDestinationCountryId());
            record.setWeight(request.getWeight());
            try {
                record.setCreatedAt(Instant.parse(request.getCreatedAt()));
            } catch (DateTimeParseException e) {
                log.error("Invalid created_at format: {}", request.getCreatedAt(), e);
                throw new InvalidTrackingRequestException("Invalid created_at format: " + request.getCreatedAt());
            }
            record.setCustomerId(request.getCustomerId());
            record.setCustomerName(request.getCustomerName());
            record.setCustomerSlug(request.getCustomerSlug());
            record.setTrackingNumber(trackingNumber);

            try {
                trackingRepository.save(record);
                log.info("Tracking record saved to MongoDB: {}", record);
            } catch (Exception e) {
                log.error("Failed to save tracking record for tracking number: {}", trackingNumber, e);
                throw new TrackingNumberGenerationException("Failed to save tracking record to database", e);
            }

            return new TrackingResponse(trackingNumber, Instant.now().toString());
        } catch (InvalidTrackingRequestException | TrackingNumberGenerationException e) {
            log.error("Failed to generate tracking number: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while generating tracking number", e);
            throw new TrackingNumberGenerationException("Unexpected error during tracking number generation", e);
        }
    }

    private String createTrackingNumber(TrackingRequest request) {
        String prefix = request.getOriginCountryId().toUpperCase() + request.getDestinationCountryId().toUpperCase();
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String randomPart;
            try {
                randomPart = generateRandomString(12 - prefix.length());
            } catch (Exception e) {
                log.error("Failed to generate random string for tracking number", e);
                throw new TrackingNumberGenerationException("Failed to generate random string", e);
            }
            String trackingNumber = (prefix + randomPart).toUpperCase();

            if (isValidTrackingNumber(trackingNumber)) {
                try {
                    boolean exists = trackingRepository.existsByTrackingNumber(trackingNumber);
                    if (!exists) {
                        meterRegistry.counter("tracking.number.generated", "origin", request.getOriginCountryId(), "destination", request.getDestinationCountryId()).increment();
                        log.info("Tracking number is unique and valid: {}", trackingNumber);
                        return trackingNumber;
                    } else {
                        meterRegistry.counter("tracking.number.collision").increment();
                        log.warn("Tracking number collision detected: {}. Retrying...", trackingNumber);
                    }
                } catch (Exception e) {
                    log.error("Failed to check tracking number existence: {}", trackingNumber, e);
                    throw new TrackingNumberGenerationException("Failed to verify tracking number uniqueness", e);
                }
            }
        }
        meterRegistry.counter("tracking.number.failure").increment();
        log.error("Failed to generate a unique tracking number after {} attempts", MAX_ATTEMPTS);
        throw new TrackingNumberGenerationException("Unable to generate unique tracking number after " + MAX_ATTEMPTS + " attempts");
    }

    private void validateRequest(TrackingRequest request) {
        if (request == null) {
            log.error("Tracking request is null");
            throw new InvalidTrackingRequestException("Tracking request cannot be null");
        }
        if (request.getOriginCountryId() == null || request.getOriginCountryId().isBlank()) {
            log.error("Invalid origin_country_id: {}", request.getOriginCountryId());
            throw new InvalidTrackingRequestException("origin_country_id cannot be null or empty");
        }
        if (request.getDestinationCountryId() == null || request.getDestinationCountryId().isBlank()) {
            log.error("Invalid destination_country_id: {}", request.getDestinationCountryId());
            throw new InvalidTrackingRequestException("destination_country_id cannot be null or empty");
        }
        if (request.getCustomerId() == null || request.getCustomerId().isBlank()) {
            log.error("Invalid customer_id: {}", request.getCustomerId());
            throw new InvalidTrackingRequestException("customer_id cannot be null or empty");
        }
        if (request.getCustomerSlug() == null || request.getCustomerSlug().isBlank()) {
            log.error("Invalid customer_slug: {}", request.getCustomerSlug());
            throw new InvalidTrackingRequestException("customer_slug cannot be null or empty");
        }
        if (!request.getOriginCountryId().matches("^[A-Z]{2}$") || !request.getDestinationCountryId().matches("^[A-Z]{2}$")) {
            log.error("Invalid country code format: origin={}, destination={}", request.getOriginCountryId(), request.getDestinationCountryId());
            throw new InvalidTrackingRequestException("Country codes must be 2-letter ISO 3166-1 alpha-2 format");
        }
        if (request.getWeight() <= 0) {
            log.error("Invalid weight: {}", request.getWeight());
            throw new InvalidTrackingRequestException("Weight must be positive");
        }
        log.info("Request validated successfully: {}", request);
    }

    private String generateRandomString(int length) {
        if (length <= 0) {
            log.error("Invalid length for random string: {}", length);
            throw new IllegalArgumentException("Length must be positive");
        }
        try {
            byte[] bytes = new byte[length];
            random.nextBytes(bytes);
            String result = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, length);
            log.debug("Generated random string: {}", result);
            return result;
        } catch (Exception e) {
            log.error("Error generating random string with length: {}", length, e);
            throw new TrackingNumberGenerationException("Failed to generate random string", e);
        }
    }

    private boolean isValidTrackingNumber(String trackingNumber) {
        if (trackingNumber == null) {
            log.error("Tracking number is null");
            return false;
        }
        boolean valid = trackingNumber.matches("^[A-Z0-9]{1,16}$");
        log.debug("Tracking number {} is valid: {}", trackingNumber, valid);
        return valid;
    }
}