package com.examplatform.modules.written.evaluation.ai.service;

import com.examplatform.modules.written.evaluation.ai.parser.TranscriptionResponseParser;
import com.examplatform.modules.written.evaluation.ai.prompt.TranscriptionPromptBuilder;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.enums.QuestionPart;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiTranscriptionService {

    private final TranscriptionPromptBuilder promptBuilder;
    private final TranscriptionResponseParser responseParser;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.1-flash-lite}")
    private String model;

    public List<TranscriptionResponseParser.TranscriptEntry> transcribe(
            List<WrittenQuestion> questions,
            Map<String, List<QuestionPart>> partsToTranscribeByQuestionId,
            List<String> base64Images,
            String mimeType) {

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        String systemInstruction = promptBuilder.buildSystemInstruction();
        String userPrompt = promptBuilder.buildUserPrompt(questions, partsToTranscribeByQuestionId);

        var partsArray = objectMapper.createArrayNode();

        var textPart = objectMapper.createObjectNode();
        textPart.put("text", systemInstruction + "\n\n" + userPrompt);
        partsArray.add(textPart);

        for (String base64Image : base64Images) {
            var imagePart = objectMapper.createObjectNode();
            var inlineData = objectMapper.createObjectNode();
            inlineData.put("mime_type", mimeType);
            inlineData.put("data", base64Image);
            imagePart.set("inline_data", inlineData);
            partsArray.add(imagePart);
        }

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
            String rawText = extractText(response);
            return responseParser.parse(rawText);
        } catch (Exception e) {
            throw new RuntimeException("Gemini transcription call failed: " + e.getMessage(), e);
        }
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
