package com.ericsson.graduate.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgramDTO {
    private long id;
    private String name;
    private String gerritName;
    private String jenkinsName;
}
