package com.examplatform.modules.exam.repository;

import java.sql.Timestamp;

public interface NotificationGroupProjection {
    String getBatchKey();
    String getSampleId();
    String getTitle();
    String getBody();
    String getType();
    Timestamp getSentAt();
    Timestamp getExpiryDate();
    Long getRecipientCount();
}
