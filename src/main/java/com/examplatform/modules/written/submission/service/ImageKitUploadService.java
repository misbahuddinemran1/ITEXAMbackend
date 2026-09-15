package com.examplatform.modules.written.submission.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.UUID;

@Service
public class ImageKitUploadService {

    @Value("${imagekit.private-key}")
    private String privateKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String UPLOAD_URL = "https://upload.imagekit.io/api/v1/files/upload";

    public String uploadFile(MultipartFile file, String folder) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            String auth = Base64.getEncoder().encodeToString((privateKey + ":").getBytes());
            headers.set("Authorization", "Basic " + auth);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            body.add("fileName", fileName);
            if (folder != null) {
                body.add("folder", folder);
            }
            body.add("useUniqueFileName", "true");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(UPLOAD_URL, requestEntity, String.class);

            JsonNode json = objectMapper.readTree(response.getBody());
            if (json.has("url")) {
                return json.get("url").asText();
            }
            throw new IllegalStateException("ImageKit response এ url পাওয়া যায়নি: " + response.getBody());

        } catch (Exception e) {
            throw new IllegalStateException("ImageKit আপলোড ব্যর্থ হয়েছে: " + e.getMessage(), e);
        }
    }
}
