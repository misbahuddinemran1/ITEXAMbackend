package com.examplatform.modules.written.evaluation.ai.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class AiFileReaderService {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Fetches a file from its public URL (ImageKit CDN) and returns it as a
     * Base64-encoded string, ready to send to Gemini as inline_data.
     */
    public String readAsBase64(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("fileUrl is null or blank, cannot read file for AI processing");
        }
        try {
            byte[] bytes = restTemplate.getForObject(fileUrl, byte[].class);
            if (bytes == null) {
                throw new IllegalStateException("Empty response while fetching file: " + fileUrl);
            }
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch file for AI processing: " + fileUrl, e);
        }
    }

    /**
     * Detects MIME type based on the file URL's extension.
     * ImageKit URLs typically end with the original file extension
     * (e.g. .../written-submissions/xxx.pdf or .../xxx.jpg).
     */
    public String detectMimeType(String fileUrl) {
        if (fileUrl == null) {
            return "application/octet-stream";
        }
        String lower = fileUrl.toLowerCase();
        // Strip query params (ImageKit sometimes appends transformation params) before checking extension
        int qIndex = lower.indexOf('?');
        if (qIndex != -1) {
            lower = lower.substring(0, qIndex);
        }

        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }
}
