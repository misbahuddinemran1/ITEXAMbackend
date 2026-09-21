package com.examplatform.modules.leaderboard.controller;

import com.examplatform.modules.leaderboard.service.OverallLeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// ছাত্রের পরিচয় আসে Bearer token থেকে (Authentication), client এর পাঠানো header থেকে নয়।
@RestController
@RequestMapping("/leaderboard")
@RequiredArgsConstructor
public class OverallLeaderboardController {

    private final OverallLeaderboardService leaderboardService;

    @GetMapping("/overall")
    public ResponseEntity<?> getOverall(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (isNotLoggedIn(auth)) return unauthorized();
        return ResponseEntity.ok(leaderboardService.getOverallLeaderboard(auth.getName(), page, size));
    }

    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthly(
            Authentication auth,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (isNotLoggedIn(auth)) return unauthorized();
        return ResponseEntity.ok(leaderboardService.getMonthlyLeaderboard(auth.getName(), yearMonth, page, size));
    }

    @GetMapping("/my-rank")
    public ResponseEntity<?> getMyRank(Authentication auth) {
        if (isNotLoggedIn(auth)) return unauthorized();
        return ResponseEntity.ok(leaderboardService.getMyOverallRank(auth.getName()));
    }

    private boolean isNotLoggedIn(Authentication auth) {
        return auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated();
    }

    private ResponseEntity<?> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("success", false, "message", "Leaderboard দেখতে login করুন"));
    }
}
