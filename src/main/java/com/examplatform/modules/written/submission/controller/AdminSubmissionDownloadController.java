package com.examplatform.modules.written.submission.controller;

import com.examplatform.modules.written.submission.entity.WrittenSubmissionFile;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionFileRepository;
import com.examplatform.modules.written.submission.response.SubmissionFileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/admin/written/submissions")
@RequiredArgsConstructor
public class AdminSubmissionDownloadController {

    private final WrittenSubmissionFileRepository submissionFileRepository;

    /**
     * Returns the raw file list (with direct CDN URLs) as JSON so the admin app can render
     * images/PDFs inline instead of following a redirect. This is the endpoint the admin app
     * should call to build an in-app answer-script viewer.
     */
    @GetMapping("/{submissionId}/files")
    public List<SubmissionFileResponse> getFiles(@PathVariable String submissionId) {
        return submissionFileRepository.findBySubmissionIdOrderByPageNumberAsc(submissionId).stream()
                .map(f -> SubmissionFileResponse.builder()
                        .id(f.getId())
                        .submissionId(f.getSubmissionId())
                        .pageNumber(f.getPageNumber())
                        .fileUrl(f.getFileUrl())
                        .fileType(f.getFileType())
                        .uploadedAt(f.getUploadedAt())
                        .build())
                .toList();
    }

    /**
     * Redirects to the first page's file URL (ImageKit CDN).
     * For single-page PDF submissions this is the whole answer script.
     */
    @GetMapping("/{submissionId}/download")
    public ResponseEntity<Void> downloadSubmission(@PathVariable String submissionId) {
        List<WrittenSubmissionFile> files = submissionFileRepository
                .findBySubmissionIdOrderByPageNumberAsc(submissionId);

        if (files.isEmpty()) {
            throw new NoSuchElementException("No files found for submission: " + submissionId);
        }

        String fileUrl = files.get(0).getFileUrl();
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalStateException("File URL missing for submission: " + submissionId);
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(fileUrl))
                .build();
    }

    /**
     * Redirects to a specific page's file URL (for multi-page image submissions).
     */
    @GetMapping("/{submissionId}/download/page/{pageNumber}")
    public ResponseEntity<Void> downloadSubmissionPage(@PathVariable String submissionId,
                                                         @PathVariable Integer pageNumber) {
        List<WrittenSubmissionFile> files = submissionFileRepository
                .findBySubmissionIdOrderByPageNumberAsc(submissionId);

        WrittenSubmissionFile file = files.stream()
                .filter(f -> f.getPageNumber().equals(pageNumber))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "Page " + pageNumber + " not found for submission: " + submissionId));

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(file.getFileUrl()))
                .build();
    }
}
