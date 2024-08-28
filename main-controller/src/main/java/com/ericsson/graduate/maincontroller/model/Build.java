package com.ericsson.graduate.maincontroller.model;

import com.ericsson.graduate.core.dto.BuildDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@EqualsAndHashCode(exclude = {"program"})
@Data
@NoArgsConstructor
@Entity
@Table(name = "builds")
public class Build {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int buildNumber;

    @NotNull
    private int totalTestsPerBuild;

    @NotNull
    private int totalPassedTestsPerBuild;

    @NotNull
    private int totalFailedTestsPerBuild;

    private LocalDateTime buildTimestamp;


    @NotNull
    @Length(max = 64)
    private String status;

    @ManyToOne(fetch = FetchType.EAGER)
    @Getter(onMethod = @__(@JsonIgnore))
    private Program program;

    public Build(BuildDTO dto) {
        setBuildNumber(dto.getBuildNumber());
        setTotalTestsPerBuild(dto.getTotalTestsPerBuild());
        setTotalPassedTestsPerBuild(dto.getTotalPassedTestsPerBuild());
        setTotalFailedTestsPerBuild(dto.getTotalFailedTestsPerBuild());
        setBuildTimestamp(dto.getBuildTimestamp());
        setStatus(dto.getStatus());
    }
}
