package com.ericsson.graduate.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private long id;
    private String signum;
    private String firstName;
    private String lastName;

    public MemberDTO(String signum, String firstName, String lastName) {
        this.signum = signum;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
