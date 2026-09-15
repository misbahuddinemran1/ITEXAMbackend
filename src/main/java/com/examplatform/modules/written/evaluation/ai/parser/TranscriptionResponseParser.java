package com.examplatform.modules.written.evaluation.ai.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TranscriptionResponseParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<TranscriptEntry> parse(String rawText) {
        try {
            String cleaned = stripMarkdownFence(rawText);
            JsonNode root = objectMapper.readTree(cleaned);
            JsonNode transcripts = root.get("transcripts");

            List<TranscriptEntry> result = new ArrayList<>();
            if (transcripts != null && transcripts.isArray()) {
                for (JsonNode node : transcripts) {
                    result.add(new TranscriptEntry(
                            node.get("questionId").asText(),
                            node.get("part").asText(),
                            node.has("transcribedText") ? node.get("transcribedText").asText() : ""
                    ));
                }
            }
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse transcription response: " + rawText, e);
        }
    }

    private String stripMarkdownFence(String text) {
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceAll("^```json", "").replaceAll("^```", "").replaceAll("```$", "");
        }
        return trimmed.trim();
    }

    public record TranscriptEntry(String questionId, String part, String transcribedText) {}
}
