package com.examplatform.modules.challenge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResultResponse {
    private String id;
    private String fullName;
    private String fullNameBn;
    private String institutionName;
    private String avatarUrl;
}
