package com.ericsson.graduate.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommitDTO {
    private String commitId;
    private String[] parents;
    private String message;
    private String authorName;
    private String authorEmail;
    private LocalDateTime time;
}
