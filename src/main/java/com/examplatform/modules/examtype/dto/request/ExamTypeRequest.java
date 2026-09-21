package com.examplatform.modules.examtype.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamTypeRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String nameBn;

    @NotBlank(message = "Code is required")
    @Size(max = 30)
    @Pattern(
        regexp = "^[A-Z0-9_]+$",
        message = "Code must be uppercase letters, numbers or underscore"
    )
    private String code;

    private String description;

    @Size(max = 200)
    private String conductingBody;

    // admin edit থেকে category আবার active/deactivate করার জন্য (optional, null হলে অপরিবর্তিত)
    private Boolean isActive;
}