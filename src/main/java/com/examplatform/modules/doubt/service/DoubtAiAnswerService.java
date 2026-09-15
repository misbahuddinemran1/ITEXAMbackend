package com.examplatform.modules.doubt.service;

import com.examplatform.modules.doubt.entity.DoubtQuestion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class DoubtAiAnswerService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.1-flash-lite}")
    private String model;

    public String generateAnswerText(DoubtQuestion doubt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        String prompt = buildPrompt(doubt);

        var partsArray = objectMapper.createArrayNode();
        var textPart = objectMapper.createObjectNode();
        textPart.put("text", prompt);
        partsArray.add(textPart);

        var contentNode = objectMapper.createObjectNode();
        contentNode.set("parts", partsArray);

        var contentsArray = objectMapper.createArrayNode();
        contentsArray.add(contentNode);

        var requestBody = objectMapper.createObjectNode();
        requestBody.set("contents", contentsArray);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        try {
            String response = restTemplate.postForObject(url, request, String.class);
            return extractText(response);
        } catch (Exception e) {
            throw new RuntimeException("Gemini doubt-answer call failed: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(DoubtQuestion doubt) {
        StringBuilder sb = new StringBuilder();
        sb.append("তুমি একজন অভিজ্ঞ SSC/HSC শিক্ষক। নিচের ছাত্রের প্রশ্নের একটি সম্পূর্ণ, স্পষ্ট, ধাপে ধাপে উত্তর বাংলায় লেখো।\n\n");
        sb.append("প্রশ্ন: ").append(doubt.getQuestionText() != null ? doubt.getQuestionText() : "(শুধু ছবি/PDF সংযুক্ত)").append("\n\n");
        if (doubt.getQuestionImageUrl() != null) {
            sb.append("(নোট: ছাত্র একটি ছবিও যুক্ত করেছে, যেটা এই মুহূর্তে বিশ্লেষণ করা হচ্ছে না)\n");
        }
        sb.append("উত্তর সহজ ভাষায়, পড়াশোনার উপযোগী ফরম্যাটে লেখো।");
        return sb.toString();
    }

    private String extractText(String responseJson) throws Exception {
        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode textNode = root
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text");

        if (textNode.isMissingNode()) {
            throw new IllegalStateException("Gemini response থেকে text পাওয়া যায়নি: " + responseJson);
        }
        return textNode.asText().trim();
    }
}
