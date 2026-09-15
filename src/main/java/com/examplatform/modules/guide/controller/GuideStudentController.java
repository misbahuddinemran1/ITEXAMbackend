package com.examplatform.modules.guide.controller;

import com.examplatform.modules.guide.dto.GuideContentResponse;
import com.examplatform.modules.guide.entity.GuideContent;
import com.examplatform.modules.guide.service.GuideContentStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("guide")
@RequiredArgsConstructor
public class GuideStudentController {

    private final GuideContentStudentService guideContentStudentService;

    // Read screen
    @GetMapping("/topics/{topicId}/content")
    public ResponseEntity<?> getContent(@PathVariable String topicId) {
        GuideContent content = guideContentStudentService.getPublishedContent(topicId);
        return ResponseEntity.ok(Map.of("success", true,
                "data", GuideContentResponse.from(content)));
    }

    // Practice Options screen — MCQ
    @GetMapping("/topics/{topicId}/mcq")
    public ResponseEntity<?> getMcq(@PathVariable String topicId) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", guideContentStudentService.getMcqForTopic(topicId)));
    }

    // Practice Options screen — Board Questions (CQ)
    @GetMapping("/topics/{topicId}/board")
    public ResponseEntity<?> getBoardQuestions(@PathVariable String topicId) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", guideContentStudentService.getBoardQuestionsForTopic(topicId)));
    }

    // Practice Options screen — Previous Year MCQ (board only)
    @GetMapping("/topics/{topicId}/mcq-board")
    public ResponseEntity<?> getMcqBoard(@PathVariable String topicId) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", guideContentStudentService.getBoardMcqForTopic(topicId)));
    }

    // Practice Options screen — CQ
    @GetMapping("/topics/{topicId}/cq")
    public ResponseEntity<?> getCq(@PathVariable String topicId) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", guideContentStudentService.getCqForTopic(topicId)));
    }
}
