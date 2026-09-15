package com.examplatform.modules.written.question.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiAnswerGeneratorService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.1-flash-lite}")
private String model;
    public GeminiAnswerGeneratorService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * প্রশ্ন থেকে একটা reference (model) answer generate করে Gemini দিয়ে
     */
    public String generateReferenceAnswer(String stimulus, String questionText, int maxMark) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        String prompt = buildPrompt(stimulus, questionText, maxMark);

        String requestBody = """
                {
                  "contents": [
                    {
                      "parts": [
                        { "text": %s }
                      ]
                    }
                  ]
                }
                """.formatted(objectMapper.valueToTree(prompt).toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {
            String response = restTemplate.postForObject(url, request, String.class);
            return extractText(response);
        } catch (Exception e) {
            throw new RuntimeException("Gemini API call failed: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(String stimulus, String questionText, int maxMark) {
        return """
                তুমি একজন অভিজ্ঞ পরীক্ষক। নিচের সৃজনশীল প্রশ্নের একটা আদর্শ উত্তর (model answer) লিখে দাও।

                উদ্দীপক: %s

                প্রশ্ন: %s

                মার্ক: %d

                শুধু উত্তরটা লিখো, কোনো ভূমিকা বা extra text ছাড়া। উত্তরটা সংক্ষিপ্ত কিন্তু সম্পূর্ণ হতে হবে, মার্ক অনুযায়ী গভীরতা বজায় রেখে।
                """.formatted(stimulus, questionText, maxMark);
    }

    private String extractText(String responseJson) throws Exception {
        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode textNode = root
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text");

        if (textNode.isMissingNode()) {
            throw new RuntimeException("Gemini response থেকে text পাওয়া যায়নি: " + responseJson);
        }
        return textNode.asText().trim();
    }
}
