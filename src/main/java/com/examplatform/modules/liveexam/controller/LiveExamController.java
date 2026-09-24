package com.examplatform.modules.liveexam.controller;
import com.examplatform.modules.liveexam.dto.*;
import com.examplatform.modules.liveexam.service.LiveExamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("live-exams")
@RequiredArgsConstructor
public class LiveExamController {

    private final LiveExamService liveExamService;
    private final JdbcTemplate jdbcTemplate; // for education_level lookup, same pattern as ExamStudentService

    // GET /api/v1/live-exams/today  — visibility list (respects class/category filter)
    @GetMapping("/today")
    public ResponseEntity<?> getTodaysLiveExams(@RequestHeader("X-User-Id") String userId) {
        try {
            String userLevel = getUserEducationLevel(userId);
            List<LiveExamSummaryResponse> data = liveExamService.getTodaysLiveExams(userLevel, userId);
            return ResponseEntity.ok(Map.of("success", true, "data", data));
        } catch (Exception ex) {
            log.error("Error fetching today's live exams", ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }
    // GET /api/v1/live-exams/category/{category}  — SUBJECT_WISE / CHAPTER_WISE / TOPIC_WISE (live+finished)
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getExamsByCategory(@PathVariable String category, @RequestHeader("X-User-Id") String userId) {
        try {
            String userLevel = getUserEducationLevel(userId);
            List<LiveExamSummaryResponse> data = liveExamService.getExamsByCategory(category, userLevel, userId);
            return ResponseEntity.ok(Map.of("success", true, "data", data));
        } catch (Exception ex) {
            log.error("Error fetching exams by category {}", category, ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    // POST /api/v1/live-exams/{examId}/start
    @PostMapping("/{examId}/start")
    public ResponseEntity<?> start(@PathVariable String examId, @RequestHeader("X-User-Id") String userId) {
        try {
            LiveExamStartResponse resp = liveExamService.startExam(examId, userId);
            return ResponseEntity.ok(Map.of("success", true, "data", resp));
        } catch (Exception ex) {
            log.error("Error starting live exam {}", examId, ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    // GET /api/v1/live-exams/{examId}/resume
    @GetMapping("/{examId}/resume")
    public ResponseEntity<?> resume(@PathVariable String examId, @RequestHeader("X-User-Id") String userId) {
        try {
            LiveExamStartResponse resp = liveExamService.resumeExam(examId, userId);
            return ResponseEntity.ok(Map.of("success", true, "data", resp));
        } catch (Exception ex) {
            log.warn("Resume failed for exam {} user {}: {}", examId, userId, ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    // POST /api/v1/live-exams/session/{sessionId}/heartbeat
    @PostMapping("/session/{sessionId}/heartbeat")
    public ResponseEntity<?> heartbeat(@PathVariable String sessionId, @RequestHeader("X-User-Id") String userId) {
        try {
            liveExamService.heartbeat(sessionId, userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    // POST /api/v1/live-exams/session/{sessionId}/disconnect
    @PostMapping("/session/{sessionId}/disconnect")
    public ResponseEntity<?> disconnect(@PathVariable String sessionId, @RequestHeader("X-User-Id") String userId) {
        try {
            liveExamService.markDisconnected(sessionId, userId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    // POST /api/v1/live-exams/session/{sessionId}/answer
    @PostMapping("/session/{sessionId}/answer")
    public ResponseEntity<?> submitAnswer(
            @PathVariable String sessionId,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody SubmitLiveAnswerRequest request) {
        try {
            liveExamService.submitAnswer(sessionId, userId, request);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception ex) {
            log.error("Error submitting answer session {}", sessionId, ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    // POST /api/v1/live-exams/session/{sessionId}/finish
    @PostMapping("/session/{sessionId}/finish")
    public ResponseEntity<?> finish(@PathVariable String sessionId, @RequestHeader("X-User-Id") String userId) {
        try {
            liveExamService.finishExam(sessionId, userId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Exam submitted. Result will be available after 11:59 PM."));
        } catch (Exception ex) {
            log.error("Error finishing exam session {}", sessionId, ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    // GET /api/v1/live-exams/{examId}/result  — time-gated
    @GetMapping("/{examId}/result")
    public ResponseEntity<?> getResult(@PathVariable String examId, @RequestHeader("X-User-Id") String userId) {
        try {
            LiveExamResultResponse result = liveExamService.getResult(examId, userId);
            return ResponseEntity.ok(Map.of("success", true, "data", result));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    // GET /api/v1/live-exams/{examId}/leaderboard  — time-gated
    @GetMapping("/{examId}/leaderboard")
    public ResponseEntity<?> getLeaderboard(@PathVariable String examId, @RequestHeader("X-User-Id") String userId) {
        try {
            List<LeaderboardEntryResponse> board = liveExamService.getLeaderboard(examId, userId);
            return ResponseEntity.ok(Map.of("success", true, "data", board));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    // GET /api/v1/live-exams/{examId}/meta  — subject/chapter/topic, no date restriction
    @GetMapping("/{examId}/meta")
    public ResponseEntity<?> getMeta(@PathVariable String examId) {
        try {
            LiveExamMetaResponse meta = liveExamService.getExamMeta(examId);
            return ResponseEntity.ok(Map.of("success", true, "data", meta));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    private String getUserEducationLevel(String userId) {
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(
                    "SELECT education_level FROM users WHERE id = ?", userId);
            if (!result.isEmpty()) return (String) result.get(0).get("education_level");
        } catch (Exception e) {
            log.warn("Could not fetch education level for user: {}", userId);
        }
        return null;
    }

    // GET /api/v1/live-exams/finished — window পার হওয়া exam list
    @GetMapping("/finished")
    public ResponseEntity<?> getFinishedExams(@RequestHeader("X-User-Id") String userId) {
        try {
            String userLevel = getUserEducationLevel(userId);
            List<LiveExamSummaryResponse> data = liveExamService.getFinishedExams(userLevel, userId);
            return ResponseEntity.ok(Map.of("success", true, "data", data));
        } catch (Exception ex) {
            log.error("Error fetching finished live exams", ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    // POST /api/v1/live-exams/{examId}/practice — stateless practice scoring
    @PostMapping("/{examId}/practice")
    public ResponseEntity<?> practiceSubmit(
            @PathVariable String examId,
            @RequestBody PracticeSubmitRequest request) {
        try {
            PracticeResultResponse result = liveExamService.practiceSubmit(examId, request.getAnswers());
            return ResponseEntity.ok(Map.of("success", true, "data", result));
        } catch (Exception ex) {
            log.error("Error in practice submit for exam {}", examId, ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @GetMapping("/{examId}/practice-questions")
public ResponseEntity<?> getPracticeQuestions(@PathVariable String examId) {
    try {
        LiveExamStartResponse resp = liveExamService.getPracticeQuestions(examId);
        return ResponseEntity.ok(Map.of("success", true, "data", resp));
    } catch (Exception ex) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
    }
}

    // GET /api/v1/live-exams/questions/hardest?minAttempts=5&limit=20
    @GetMapping("/questions/hardest")
    public ResponseEntity<?> getHardestQuestions(
            @RequestParam(defaultValue = "5") int minAttempts,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<QuestionStatsResponse> data = liveExamService.getHardestQuestions(minAttempts, limit);
            return ResponseEntity.ok(Map.of("success", true, "data", data));
        } catch (Exception ex) {
            log.error("Error fetching hardest questions", ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    // GET /api/v1/live-exams/my-attempts?onlyWrong=true
    @GetMapping("/my-attempts")
    public ResponseEntity<?> getMyAttemptHistory(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) Boolean onlyWrong) {
        try {
            List<UserQuestionAttemptResponse> data = liveExamService.getUserAttemptHistory(userId, onlyWrong, true);
            return ResponseEntity.ok(Map.of("success", true, "data", data));
        } catch (Exception ex) {
            log.error("Error fetching attempt history for user {}", userId, ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    // GET /api/v1/live-exams/revision-questions?limit=20
    @GetMapping("/revision-questions")
    public ResponseEntity<?> getRevisionQuestions(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) Integer limit) {
        try {
            RevisionQuizResponse data = liveExamService.getRevisionQuestions(userId, limit);
            return ResponseEntity.ok(Map.of("success", true, "data", data));
        } catch (Exception ex) {
            log.error("Error building revision quiz for user {}", userId, ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    // POST /api/v1/live-exams/revision-submit
    @PostMapping("/revision-submit")
    public ResponseEntity<?> submitRevision(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody RevisionSubmitRequest request) {
        try {
            RevisionResultResponse result = liveExamService.submitRevision(request);
            return ResponseEntity.ok(Map.of("success", true, "data", result));
        } catch (Exception ex) {
            log.error("Error scoring revision submit for user {}", userId, ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    // GET /api/v1/live-exams/subject-accuracy
    @GetMapping("/subject-accuracy")
    public ResponseEntity<?> getSubjectAccuracy(@RequestHeader("X-User-Id") String userId) {
        try {
            List<SubjectAccuracyResponse> data = liveExamService.getSubjectAccuracy(userId);
            return ResponseEntity.ok(Map.of("success", true, "data", data));
        } catch (Exception ex) {
            log.error("Error fetching subject accuracy for user {}", userId, ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    // GET /api/v1/live-exams/chapter-accuracy
    @GetMapping("/chapter-accuracy")
    public ResponseEntity<?> getChapterAccuracy(@RequestHeader("X-User-Id") String userId) {
        try {
            List<ChapterAccuracyResponse> data = liveExamService.getChapterAccuracy(userId);
            return ResponseEntity.ok(Map.of("success", true, "data", data));
        } catch (Exception ex) {
            log.error("Error fetching chapter accuracy for user {}", userId, ex);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }
}
