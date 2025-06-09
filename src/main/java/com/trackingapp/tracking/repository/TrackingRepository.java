package com.trackingapp.tracking.repository;

import com.trackingapp.tracking.model.TrackingRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackingRepository extends MongoRepository<TrackingRecord, String> {

    // This must match the field name exactly: "trackingNumber"
    boolean existsByTrackingNumber(String trackingNumber);
}