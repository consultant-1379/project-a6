package com.ericsson.graduate.microservices.gerrit.data;

import com.ericsson.graduate.microservices.gerrit.json.LocalDateTimeDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommitAuthor {
    private String name;
    private String email;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime time;
}
