package com.examplatform.modules.examtype.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExamTypeResponse {
    private String id;
    private String name;
    private String nameBn;
    private String code;
    private String description;
    private String conductingBody;

    // Lombok এর "isActive" getter কে Jackson "active" বানিয়ে দিত (is-prefix strip করে),
    // ফলে JSON এ "isActive" key-ই আসত না। তাই এখানে @Getter বন্ধ করে ম্যানুয়াল
    // getter + @JsonProperty দিয়ে JSON key জোর করে "isActive" রাখা হলো।
    @Getter(AccessLevel.NONE)
    private boolean isActive;

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }
}