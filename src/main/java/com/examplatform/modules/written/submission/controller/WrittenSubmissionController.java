package com.examplatform.modules.written.submission.controller;

import com.examplatform.modules.written.submission.enums.FileType;
import com.examplatform.modules.written.submission.request.StartExamRequest;
import com.examplatform.modules.written.submission.request.SubmitExamRequest;
import com.examplatform.modules.written.submission.request.SubmitTextAnswersRequest;
import com.examplatform.modules.written.submission.request.UploadSubmissionFileRequest;
import com.examplatform.modules.written.submission.response.SubmissionFileResponse;
import com.examplatform.modules.written.submission.response.SubmissionResponse;
import com.examplatform.modules.written.submission.service.ImageKitUploadService;
import com.examplatform.modules.written.submission.service.WrittenSubmissionService;
import com.examplatform.modules.written.submission.entity.WrittenSubmissionTranscript;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionTranscriptRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/written/submissions")
@RequiredArgsConstructor
public class WrittenSubmissionController {

    private final WrittenSubmissionService submissionService;
    private final WrittenSubmissionTranscriptRepository transcriptRepository;
    private final ImageKitUploadService imageKitUploadService;

    @PostMapping("/start")
    public SubmissionResponse startExam(@RequestBody StartExamRequest request, Authentication auth) {
        return submissionService.startExam(auth.getName(), request);
    }

    @PostMapping(value = "/{submissionId}/upload", consumes = "multipart/form-data")
    public SubmissionFileResponse uploadFile(
            @PathVariable String submissionId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("pageNumber") Integer pageNumber,
            @RequestParam("fileType") FileType fileType,
            Authentication auth) {

        String fileUrl = imageKitUploadService.uploadFile(file, "written-submissions/" + submissionId);

        UploadSubmissionFileRequest request = new UploadSubmissionFileRequest();
        request.setPageNumber(pageNumber);
        request.setFileUrl(fileUrl);
        request.setFileType(fileType);

        return submissionService.uploadFile(submissionId, auth.getName(), request);
    }

    @PostMapping("/{submissionId}/submit-text")
    public void submitTextAnswers(
            @PathVariable String submissionId,
            @RequestBody SubmitTextAnswersRequest request,
            Authentication auth) {
        submissionService.submitTextAnswers(submissionId, auth.getName(), request);
    }

    @PostMapping("/{submissionId}/submit")
    public SubmissionResponse submitExam(
            @PathVariable String submissionId,
            @RequestBody SubmitExamRequest request,
            Authentication auth) {
        return submissionService.submitExam(submissionId, auth.getName(), request);
    }

    @GetMapping("/{submissionId}")
    public SubmissionResponse getSubmission(@PathVariable String submissionId, Authentication auth) {
        return submissionService.getSubmissionById(submissionId, auth.getName());
    }

    @GetMapping("/{submissionId}/files")
    public List<SubmissionFileResponse> getFiles(@PathVariable String submissionId, Authentication auth) {
        return submissionService.getSubmissionFiles(submissionId, auth.getName());
    }

    @GetMapping("/my")
    public List<SubmissionResponse> getMySubmissions(Authentication auth) {
        return submissionService.getMySubmissions(auth.getName());
    }

    /**
     * Student's own answer script — their typed/transcribed text per question+part.
     * Ownership is enforced by first calling getSubmissionById, which already throws if this
     * submission doesn't belong to the logged-in student.
     */
    @GetMapping("/{submissionId}/transcript")
    public List<Map<String, Object>> getMyTranscript(@PathVariable String submissionId, Authentication auth) {
        submissionService.getSubmissionById(submissionId, auth.getName()); // ownership check, throws if not owner

        List<WrittenSubmissionTranscript> transcripts = transcriptRepository.findBySubmissionId(submissionId);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (WrittenSubmissionTranscript t : transcripts) {
            Map<String, Object> item = new HashMap<>();
            item.put("questionId", t.getQuestion().getId());
            item.put("questionOrder", t.getQuestion().getQuestionOrder());
            item.put("part", t.getPart().name());
            item.put("transcribedText", t.getTranscribedText());
            result.add(item);
        }
        return result;
    }
}
