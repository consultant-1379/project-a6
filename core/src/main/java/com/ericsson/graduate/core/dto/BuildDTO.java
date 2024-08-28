package com.ericsson.graduate.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildDTO {
    private int buildNumber;
    private int totalTestsPerBuild;
    private int totalPassedTestsPerBuild;
    private int totalFailedTestsPerBuild;
    private LocalDateTime buildTimestamp;
    private String status;

}
