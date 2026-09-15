package com.examplatform.modules.written.submission.response;

import com.examplatform.modules.written.submission.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionFileResponse {

    private String id;
    private String submissionId;
    private Integer pageNumber;
    private String fileUrl;
    private FileType fileType;
    private LocalDateTime uploadedAt;
}