package com.examplatform.modules.guide.controller;

import com.examplatform.modules.guide.dto.GuideContentAdminRequest;
import com.examplatform.modules.guide.dto.GuideContentResponse;
import com.examplatform.modules.guide.entity.GuideContent;
import com.examplatform.modules.guide.service.GuideContentAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("admin/guide")
@RequiredArgsConstructor
public class GuideAdminController {

    private final GuideContentAdminService guideContentAdminService;

    @GetMapping("/contents")
    public ResponseEntity<?> getAll() {
        List<GuideContentResponse> data = guideContentAdminService.getAll()
                .stream().map(GuideContentResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @GetMapping("/contents/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        GuideContent content = guideContentAdminService.getById(id);
        return ResponseEntity.ok(Map.of("success", true, "data", GuideContentResponse.from(content)));
    }

    @PostMapping("/contents")
    public ResponseEntity<?> create(@RequestBody GuideContentAdminRequest req) {
        try {
            GuideContent content = guideContentAdminService.create(req);
            return ResponseEntity.ok(Map.of("success", true, "data", GuideContentResponse.from(content)));
        } catch (Exception ex) {
            log.error("Error creating guide content", ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @PutMapping("/contents/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody GuideContentAdminRequest req) {
        try {
            GuideContent content = guideContentAdminService.update(id, req);
            return ResponseEntity.ok(Map.of("success", true, "data", GuideContentResponse.from(content)));
        } catch (Exception ex) {
            log.error("Error updating guide content {}", id, ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @PutMapping("/contents/{id}/publish")
    public ResponseEntity<?> publish(@PathVariable String id) {
        GuideContent content = guideContentAdminService.publish(id);
        return ResponseEntity.ok(Map.of("success", true, "data", GuideContentResponse.from(content)));
    }

    @PutMapping("/contents/{id}/archive")
    public ResponseEntity<?> archive(@PathVariable String id) {
        GuideContent content = guideContentAdminService.archive(id);
        return ResponseEntity.ok(Map.of("success", true, "data", GuideContentResponse.from(content)));
    }

    @DeleteMapping("/contents/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        guideContentAdminService.delete(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Deleted"));
    }
}
