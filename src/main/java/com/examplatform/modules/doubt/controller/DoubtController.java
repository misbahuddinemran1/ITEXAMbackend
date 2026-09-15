package com.examplatform.modules.doubt.controller;

import com.examplatform.modules.doubt.dto.*;
import com.examplatform.modules.doubt.service.DoubtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doubts")
@RequiredArgsConstructor
public class DoubtController {

    private final DoubtService doubtService;

    @PostMapping
    public DoubtResponse createDoubt(@RequestBody CreateDoubtRequest request, Authentication auth) {
        return doubtService.createDoubt(auth.getName(), request);
    }

    @PostMapping(value = "/{id}/upload", consumes = "multipart/form-data")
    public Map<String, String> uploadFile(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("isPdf") boolean isPdf,
            Authentication auth) {
        String fileUrl = doubtService.uploadQuestionFile(id, auth.getName(), file, isPdf);
        return Map.of("fileUrl", fileUrl);
    }

    @PutMapping("/{id}")
    public DoubtResponse updateDoubt(
            @PathVariable String id,
            @RequestBody UpdateDoubtRequest request,
            Authentication auth) {
        return doubtService.updateDoubt(id, auth.getName(), request);
    }

    @GetMapping("/my")
    public List<DoubtSummaryResponse> getMyDoubts(Authentication auth) {
        return doubtService.getMyDoubts(auth.getName());
    }

    @GetMapping("/answered")
    public List<DoubtSummaryResponse> getAnsweredDoubts(
            @RequestParam(required = false) String chapterId,
            @RequestParam(required = false) String subjectId) {
        return doubtService.getAnsweredDoubts(chapterId, subjectId);
    }

    @GetMapping("/{id}")
    public DoubtResponse getDoubtDetail(@PathVariable String id, Authentication auth) {
        return doubtService.getDoubtDetail(id, auth.getName());
    }
}
