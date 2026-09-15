package com.examplatform.modules.written.submission.request;

import com.examplatform.modules.written.submission.enums.FileType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadSubmissionFileRequest {
    private Integer pageNumber;
    private String fileUrl;
    private FileType fileType;
}