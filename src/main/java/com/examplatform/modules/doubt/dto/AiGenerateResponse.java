package com.examplatform.modules.doubt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiGenerateResponse {
    private String generatedText;
    private String generatedPdfUrl; // preview pdf, save না করা পর্যন্ত temporary/draft হতে পারে
}
