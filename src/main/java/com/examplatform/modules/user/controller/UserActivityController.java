package com.examplatform.modules.user.controller;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.infrastructure.security.JwtTokenProvider;
import com.examplatform.modules.user.dto.UserExamSummaryResponse;
import com.examplatform.modules.user.service.UserExamSummaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/activity")
@RequiredArgsConstructor
@Tag(name = "User Activity", description = "ছাত্রের নিজের পরীক্ষার সারসংক্ষেপ")
@SecurityRequirement(name = "Bearer Authentication")
public class UserActivityController {

    private final UserExamSummaryService userExamSummaryService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/summary")
    @Operation(summary = "নিজে মোট কতগুলো পরীক্ষা দিয়েছি")
    public ResponseEntity<ApiResponse<UserExamSummaryResponse>> mySummary(
            HttpServletRequest request) {
        String userId = extractUserId(request);
        return ResponseEntity.ok(
                ApiResponse.success("Exam summary fetched",
                        userExamSummaryService.getSummary(userId))
        );
    }

    // UserProfileController এর মতোই: পরিচয় নেওয়া হয় Bearer token থেকে, client এর পাঠানো header থেকে নয়
    private String extractUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new com.examplatform.common.exception.ValidationException(
                "Authorization token দিতে হবে");
        }
        String token = authHeader.substring(7);
        return jwtTokenProvider.getUsernameFromToken(token);
    }
}
