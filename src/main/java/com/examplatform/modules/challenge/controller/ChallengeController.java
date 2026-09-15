package com.examplatform.modules.challenge.controller;

import com.examplatform.modules.challenge.dto.*;
import com.examplatform.modules.challenge.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping("/friend")
    public ResponseEntity<?> createFriend(@RequestHeader("X-User-Id") String userId,
                                           @RequestBody CreateFriendChallengeRequest req) {
        try {
            return ResponseEntity.ok(Map.of("success", true,
                    "data", challengeService.createFriendChallenge(userId, req)));
        } catch (Exception e) {
            log.error("createFriend failed", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    
    @PostMapping("/{id}/accept")
    public ResponseEntity<?> accept(@RequestHeader("X-User-Id") String userId, @PathVariable String id) {
        try {
            return ResponseEntity.ok(Map.of("success", true,
                    "data", challengeService.acceptChallenge(userId, id)));
        } catch (Exception e) {
            log.error("accept failed", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

        @PutMapping("/{id}")
    public ResponseEntity<?> editFriend(@RequestHeader("X-User-Id") String userId,
                                         @PathVariable String id,
                                         @RequestBody CreateFriendChallengeRequest req) {
        try {
            return ResponseEntity.ok(Map.of("success", true,
                    "data", challengeService.editFriendChallenge(userId, id, req)));
        } catch (Exception e) {
            log.error("editFriend failed", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFriend(@RequestHeader("X-User-Id") String userId, @PathVariable String id) {
        try {
            challengeService.deleteFriendChallenge(userId, id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("deleteFriend failed", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    @PostMapping("/{id}/decline")
    public ResponseEntity<?> decline(@RequestHeader("X-User-Id") String userId, @PathVariable String id) {
        try {
            challengeService.declineChallenge(userId, id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            log.error("decline failed", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/quick-match")
    public ResponseEntity<?> quickMatch(@RequestHeader("X-User-Id") String userId,
                                         @RequestBody QuickMatchRequest req) {
        try {
            return ResponseEntity.ok(Map.of("success", true,
                    "data", challengeService.quickMatch(userId, req)));
        } catch (Exception e) {
            log.error("quickMatch failed", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/match-status")
    public ResponseEntity<?> matchStatus(@RequestHeader("X-User-Id") String userId, @PathVariable String id) {
        try {
            return ResponseEntity.ok(Map.of("success", true,
                    "data", challengeService.checkMatchStatus(userId, id)));
        } catch (Exception e) {
            log.error("matchStatus failed", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetail(@RequestHeader("X-User-Id") String userId, @PathVariable String id) {
        try {
            return ResponseEntity.ok(Map.of("success", true,
                    "data", challengeService.getChallengeDetail(userId, id)));
        } catch (Exception e) {
            log.error("getDetail failed", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/attempt")
    public ResponseEntity<?> submitAttempt(@RequestHeader("X-User-Id") String userId,
                                            @PathVariable String id,
                                            @RequestBody SubmitAttemptRequest req) {
        try {
            return ResponseEntity.ok(Map.of("success", true,
                    "data", challengeService.submitAttempt(userId, id, req)));
        } catch (Exception e) {
            log.error("submitAttempt failed", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<?> getResult(@RequestHeader("X-User-Id") String userId, @PathVariable String id) {
        try {
            return ResponseEntity.ok(Map.of("success", true,
                    "data", challengeService.getResult(userId, id)));
        } catch (Exception e) {
            log.error("getResult failed", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/search-friends")
    public ResponseEntity<?> searchFriends(@RequestHeader("X-User-Id") String userId,
                                            @RequestParam String q) {
        try {
            return ResponseEntity.ok(Map.of("success", true,
                    "data", challengeService.searchFriends(userId, q)));
        } catch (Exception e) {
            log.error("searchFriends failed", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/mine")
    public ResponseEntity<?> getMine(@RequestHeader("X-User-Id") String userId,
                                      @RequestParam(required = false) String status) {
        try {
            return ResponseEntity.ok(Map.of("success", true,
                    "data", challengeService.getMyChallenges(userId, status)));
        } catch (Exception e) {
            log.error("getMine failed", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<?> getLeaderboard(@RequestParam(defaultValue = "50") int limit) {
        try {
            return ResponseEntity.ok(Map.of("success", true,
                    "data", challengeService.getLeaderboard(limit)));
        } catch (Exception e) {
            log.error("getLeaderboard failed", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/my-stats")
    public ResponseEntity<?> getMyStats(@RequestHeader("X-User-Id") String userId) {
        try {
            return ResponseEntity.ok(Map.of("success", true,
                    "data", challengeService.getMyStats(userId)));
        } catch (Exception e) {
            log.error("getMyStats failed", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
