package com.examplatform.modules.guide.controller;

import com.examplatform.modules.guide.dto.GuidePracticeCqAdminRequest;
import com.examplatform.modules.guide.dto.GuidePracticeMcqAdminRequest;
import com.examplatform.modules.guide.service.GuidePracticeAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("admin/guide/practice")
@RequiredArgsConstructor
public class GuidePracticeAdminController {

    private final GuidePracticeAdminService guidePracticeAdminService;

    // ================= MCQ =================

    @GetMapping("/mcq")
    public ResponseEntity<?> listMcq(@RequestParam(required = false) String topicId) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", guidePracticeAdminService.listMcq(topicId)));
    }

    @PostMapping("/mcq")
    public ResponseEntity<?> createMcq(@RequestBody GuidePracticeMcqAdminRequest req) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", guidePracticeAdminService.createMcq(req)));
    }

    @PutMapping("/mcq/{id}")
    public ResponseEntity<?> updateMcq(@PathVariable String id, @RequestBody GuidePracticeMcqAdminRequest req) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", guidePracticeAdminService.updateMcq(id, req)));
    }

    @DeleteMapping("/mcq/{id}")
    public ResponseEntity<?> deleteMcq(@PathVariable String id) {
        guidePracticeAdminService.deleteMcq(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // ================= CQ =================

    @GetMapping("/cq")
    public ResponseEntity<?> listCq(@RequestParam(required = false) String topicId) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", guidePracticeAdminService.listCq(topicId)));
    }

    @PostMapping("/cq")
    public ResponseEntity<?> createCq(@RequestBody GuidePracticeCqAdminRequest req) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", guidePracticeAdminService.createCq(req)));
    }

    @PutMapping("/cq/{id}")
    public ResponseEntity<?> updateCq(@PathVariable String id, @RequestBody GuidePracticeCqAdminRequest req) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", guidePracticeAdminService.updateCq(id, req)));
    }

    @DeleteMapping("/cq/{id}")
    public ResponseEntity<?> deleteCq(@PathVariable String id) {
        guidePracticeAdminService.deleteCq(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
