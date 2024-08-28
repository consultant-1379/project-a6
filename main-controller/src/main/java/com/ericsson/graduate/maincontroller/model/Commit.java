package com.ericsson.graduate.maincontroller.model;

import com.ericsson.graduate.core.dto.CommitDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@EqualsAndHashCode(exclude = {"program"})
@Data
@NoArgsConstructor
@Entity
@Table(name = "commits")
public class Commit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String commitId;

    @Lob
    private String message;
    private String authorName;
    private String authorEmail;
    private LocalDateTime time;

    @ManyToOne
    @Getter(onMethod = @__(@JsonIgnore))
    private Program program;

    public Commit(CommitDTO dto) {
        setCommitId(dto.getCommitId());
        setMessage(dto.getMessage());
        setAuthorName(dto.getAuthorName());
        setAuthorEmail(dto.getAuthorEmail());
        setTime(dto.getTime());
    }
}
